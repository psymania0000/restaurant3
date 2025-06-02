package com.restaurant.repository;

import com.restaurant.entity.Review;
import com.restaurant.entity.Restaurant;
import com.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRestaurant(Restaurant restaurant);
    List<Review> findByRestaurantOrderByCreatedAtDesc(Restaurant restaurant);
    double findAverageRatingByRestaurant(Restaurant restaurant);
    boolean existsByUserAndRestaurant(User user, Restaurant restaurant);
    List<Review> findByRestaurantId(Long restaurantId);
    List<Review> findByUserId(Long userId);
} 