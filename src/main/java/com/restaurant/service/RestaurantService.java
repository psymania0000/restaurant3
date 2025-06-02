package com.restaurant.service;

import com.restaurant.dto.RestaurantDto;
import com.restaurant.entity.Restaurant;
import com.restaurant.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Transactional
    public RestaurantDto createRestaurant(RestaurantDto restaurantDto) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(restaurantDto.getName());
        restaurant.setAddress(restaurantDto.getAddress());
        restaurant.setPhone(restaurantDto.getPhone());
        restaurant.setDescription(restaurantDto.getDescription());
        restaurant.setImageUrl(restaurantDto.getImageUrl());

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return convertToDto(savedRestaurant);
    }

    @Transactional
    public RestaurantDto updateRestaurant(Long id, RestaurantDto restaurantDto) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        restaurant.setName(restaurantDto.getName());
        restaurant.setAddress(restaurantDto.getAddress());
        restaurant.setPhone(restaurantDto.getPhone());
        restaurant.setDescription(restaurantDto.getDescription());
        if (restaurantDto.getImageUrl() != null) {
            restaurant.setImageUrl(restaurantDto.getImageUrl());
        }

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return convertToDto(updatedRestaurant);
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
        restaurantRepository.delete(restaurant);
    }

    public List<RestaurantDto> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantDto getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + id));
        return convertToDto(restaurant);
    }

    // 레스토랑 관리자 이메일로 레스토랑 정보 조회
    @Transactional(readOnly = true)
    public RestaurantDto getRestaurantByManagerEmail(String email) {
        Restaurant restaurant = restaurantRepository.findByManagerEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found for manager email: " + email));
        return convertToDto(restaurant);
    }

    private RestaurantDto convertToDto(Restaurant restaurant) {
        RestaurantDto dto = new RestaurantDto();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setAddress(restaurant.getAddress());
        dto.setPhone(restaurant.getPhone());
        dto.setDescription(restaurant.getDescription());
        dto.setImageUrl(restaurant.getImageUrl());
        return dto;
    }
} 