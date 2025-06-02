package com.restaurant.repository;

import com.restaurant.entity.Reservation;
import com.restaurant.entity.Restaurant;
import com.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByRestaurant(Restaurant restaurant);
    List<Reservation> findByRestaurantAndReservationTimeBetween(
        Restaurant restaurant, LocalDateTime start, LocalDateTime end);
    List<Reservation> findByUserAndStatus(User user, String status);
    List<Reservation> findByRestaurantIdAndReservationTimeBetween(Long restaurantId, LocalDateTime start, LocalDateTime end);
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Reservation> findByRestaurantIdOrderByReservationTimeAsc(Long restaurantId);
    List<Reservation> findByRestaurantId(Long restaurantId);
    List<Reservation> findByRestaurantIdAndStatus(Long restaurantId, String status);
    List<Reservation> findByUserId(Long userId);
} 