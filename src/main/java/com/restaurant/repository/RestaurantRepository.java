package com.restaurant.repository;

import com.restaurant.entity.Restaurant;
import com.restaurant.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByEmail(String email);
    List<Restaurant> findByEmailIn(List<String> emails);
    List<Restaurant> findByManager(User manager);
} 