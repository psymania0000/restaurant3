package com.restaurant.service;

import com.restaurant.dto.ReviewDTO;
import com.restaurant.entity.Review;
import com.restaurant.entity.User;
import com.restaurant.entity.Restaurant;
import com.restaurant.repository.ReviewRepository;
import com.restaurant.repository.UserRepository;
import com.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO, Long restaurantId, String username) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("레스토랑을 찾을 수 없습니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        Review review = Review.builder()
                .content(reviewDTO.getContent())
                .rating(reviewDTO.getRating())
                .restaurant(restaurant)
                .user(user)
                .build();

        Review savedReview = reviewRepository.save(review);
        return convertToDTO(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReviewDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));
        return convertToDTO(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getRestaurantReviews(Long restaurantId) {
        return reviewRepository.findByRestaurantId(restaurantId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByRestaurantId(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("레스토랑을 찾을 수 없습니다."));
        return reviewRepository.findByRestaurant(restaurant).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getUserReviews(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        return reviewRepository.findByUserWithRestaurant(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewDTO updateReview(Long id, ReviewDTO reviewDTO, String username) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));
        
        if (!review.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("리뷰를 수정할 권한이 없습니다.");
        }

        review.setContent(reviewDTO.getContent());
        review.setRating(reviewDTO.getRating());
        
        Review updatedReview = reviewRepository.save(review);
        return convertToDTO(updatedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        return reviewRepository.findByUserWithRestaurant(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getReviewCount() {
        return reviewRepository.count();
    }

    @Transactional
    public void deleteReview(Long reviewId, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));
        
        if (!review.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("리뷰를 삭제할 권한이 없습니다.");
        }
        
        try {
            reviewRepository.deleteById(reviewId);
        } catch (Exception e) {
            throw new RuntimeException("리뷰 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteReview(Long reviewId, String username, java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));
        
        boolean isAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        
        if (!isAdmin && !review.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("리뷰를 삭제할 권한이 없습니다.");
        }
        
        try {
            reviewRepository.deleteById(reviewId);
        } catch (Exception e) {
            throw new RuntimeException("리뷰 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private ReviewDTO convertToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .restaurantId(review.getRestaurant().getId())
                .restaurantName(review.getRestaurant().getName())
                .author(review.getUser().getUsername())
                .userId(review.getUser().getId())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
} 