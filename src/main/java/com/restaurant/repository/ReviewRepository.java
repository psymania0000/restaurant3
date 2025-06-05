package com.restaurant.repository;

import com.restaurant.entity.Review;
import com.restaurant.entity.Restaurant;
import com.restaurant.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRestaurant(Restaurant restaurant);
    List<Review> findByRestaurantOrderByCreatedAtDesc(Restaurant restaurant);
    double findAverageRatingByRestaurant(Restaurant restaurant);
    boolean existsByUserAndRestaurant(User user, Restaurant restaurant);
    List<Review> findByRestaurantId(Long restaurantId);
    List<Review> findByUserId(Long userId);
    List<Review> findAllByOrderByCreatedAtDesc();
    List<Review> findByUser(User user);
} 