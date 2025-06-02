package com.restaurant.service;

import com.restaurant.dto.ReviewDto;
import com.restaurant.entity.Restaurant;
import com.restaurant.entity.Review;
import com.restaurant.entity.User;
import com.restaurant.repository.RestaurantRepository;
import com.restaurant.repository.ReviewRepository;
import com.restaurant.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewDto createReview(ReviewDto reviewDto) {
        Restaurant restaurant = restaurantRepository.findById(reviewDto.getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        User user = userRepository.findById(reviewDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Review review = new Review();
        review.setRestaurant(restaurant);
        review.setUser(user);
        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        // 리뷰 작성자에게 포인트 지급 (예: 50 포인트)
        user.setPoints(user.getPoints() + 50);
        userRepository.save(user);

        return convertToDto(savedReview);
    }

    public List<ReviewDto> getReviewsByRestaurantId(Long restaurantId) {
        return reviewRepository.findByRestaurantId(restaurantId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 후기 삭제 메소드
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + reviewId));
        reviewRepository.delete(review);
    }

    // 특정 후기 ID로 후기 정보를 가져오는 메소드
    @Transactional(readOnly = true)
    public ReviewDto getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + reviewId));
        return convertToDto(review);
    }

    // TODO: Add methods for updating, getting reviews by user, etc.

    private ReviewDto convertToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setRestaurantId(review.getRestaurant().getId());
        dto.setUserId(review.getUser().getId());
        dto.setUserName(review.getUser().getName()); // Assuming User entity has getName()
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
} 