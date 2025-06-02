package com.restaurant.service;

import com.restaurant.dto.ReviewDto;
import com.restaurant.entity.Review;
import com.restaurant.entity.Restaurant;
import com.restaurant.model.User;
import com.restaurant.model.Role;
import com.restaurant.repository.ReviewRepository;
import com.restaurant.repository.UserRepository;
import com.restaurant.repository.RestaurantRepository;
import com.restaurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserService userService;

    @Transactional
    public ReviewDto createReview(ReviewDto reviewDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Restaurant restaurant = restaurantRepository.findById(reviewDto.getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + reviewDto.getRestaurantId()));

        if (reviewRepository.existsByUserAndRestaurant(user, restaurant)) {
            throw new RuntimeException("이미 리뷰를 작성했습니다.");
        }

        Review review = new Review();
        review.setRestaurant(restaurant);
        review.setUser(user);
        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());

        Review savedReview = reviewRepository.save(review);

        // 리뷰 작성 시 사용자에게 포인트 지급
        userService.addPoints(userId, 50); // 50 포인트 지급

        return convertToDto(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByRestaurantId(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + restaurantId));
        return reviewRepository.findByRestaurantOrderByCreatedAtDesc(restaurant).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReviewDto getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + reviewId));
        return convertToDto(review);
    }

    @Transactional
    public ReviewDto updateReview(Long reviewId, ReviewDto reviewDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + reviewId));

        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());

        Review updatedReview = reviewRepository.save(review);
        return convertToDto(updatedReview);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new EntityNotFoundException("Review not found with id: " + reviewId);
        }
        // TODO: 리뷰 삭제 시 포인트 회수 로직 필요 시 추가
        reviewRepository.deleteById(reviewId);
    }

    private ReviewDto convertToDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .restaurantId(review.getRestaurant().getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getName())
                .comment(review.getComment())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .build();
    }

    // ReviewDto에 @Builder가 추가되었을 것으로 예상하고 사용. 만약 오류 발생 시 ReviewDto 수정 필요.
} 