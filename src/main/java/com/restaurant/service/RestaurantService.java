package com.restaurant.service;

import com.restaurant.dto.RestaurantDTO;
import com.restaurant.entity.Restaurant;
import com.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final String uploadDir = "src/main/resources/static/images/restaurants";

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @Transactional
    public RestaurantDTO createRestaurant(RestaurantDTO restaurantDTO, MultipartFile image) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(restaurantDTO.getName());
        restaurant.setAddress(restaurantDTO.getAddress());
        restaurant.setPhone(restaurantDTO.getPhone());
        restaurant.setEmail(restaurantDTO.getEmail());
        restaurant.setDescription(restaurantDTO.getDescription());
        restaurant.setCategory(restaurantDTO.getCategory());
        restaurant.setBusinessHours(restaurantDTO.getBusinessHours());
        restaurant.setMaxCapacity(restaurantDTO.getMaxCapacity());
        restaurant.setOpen(true);
        restaurant.setAdminEmail(restaurantDTO.getAdminEmail());
        restaurant.setReservationInterval(restaurantDTO.getReservationInterval() != null ? restaurantDTO.getReservationInterval() : 30);

        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = saveImage(image);
                restaurant.setImageUrl(imageUrl);
            } catch (Exception e) {
                throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
            }
        }

        try {
            Restaurant savedRestaurant = restaurantRepository.save(restaurant);
            return convertToDTO(savedRestaurant);
        } catch (Exception e) {
            throw new RuntimeException("식당 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public RestaurantDTO updateRestaurantInfo(Long id, String name, String address, String phone, String email) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("레스토랑을 찾을 수 없습니다."));

        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setPhone(phone);
        restaurant.setEmail(email);

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return convertToDTO(updatedRestaurant);
    }

    private String saveImage(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/images/restaurants/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    private RestaurantDTO convertToDTO(Restaurant restaurant) {
        RestaurantDTO dto = new RestaurantDTO();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setAddress(restaurant.getAddress());
        dto.setPhone(restaurant.getPhone());
        dto.setEmail(restaurant.getEmail());
        dto.setDescription(restaurant.getDescription());
        dto.setImageUrl(restaurant.getImageUrl());
        dto.setBusinessHours(restaurant.getBusinessHours());
        dto.setMaxCapacity(restaurant.getMaxCapacity());
        dto.setCategory(restaurant.getCategory());
        dto.setOpen(restaurant.isOpen());
        dto.setCreatedAt(restaurant.getCreatedAt());
        dto.setUpdatedAt(restaurant.getUpdatedAt());
        dto.setAdminEmail(restaurant.getAdminEmail());
        dto.setReservationInterval(restaurant.getReservationInterval());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<RestaurantDTO> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantDTO getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
        return convertToDTO(restaurant);
    }

    @Transactional
    public RestaurantDTO updateRestaurant(Long id, RestaurantDTO restaurantDTO, MultipartFile image) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        restaurant.setName(restaurantDTO.getName());
        restaurant.setDescription(restaurantDTO.getDescription());
        restaurant.setAddress(restaurantDTO.getAddress());
        restaurant.setPhone(restaurantDTO.getPhone());
        restaurant.setEmail(restaurantDTO.getEmail());

        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImage(image);
            restaurant.setImageUrl(imageUrl);
        }

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return convertToDTO(updatedRestaurant);
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public RestaurantDTO getRestaurantByManagerEmail(String email) {
        Restaurant restaurant = restaurantRepository.findByManagerEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
        return convertToDTO(restaurant);
    }

    @Transactional(readOnly = true)
    public Long getRestaurantIdByAdminEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("관리자 이메일이 제공되지 않았습니다.");
        }
        
        Restaurant restaurant = restaurantRepository.findByAdminEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("관리자 이메일(" + email + ")로 등록된 식당을 찾을 수 없습니다."));
        
        return restaurant.getId();
    }

    @Transactional(readOnly = true)
    public long getRestaurantCount() {
        return restaurantRepository.count();
    }
} 