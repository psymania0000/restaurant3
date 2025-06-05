package com.restaurant.repository;

import com.restaurant.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findAllByOrderByCreatedAtDesc();
    List<Menu> findByRestaurantId(Long restaurantId);
} 