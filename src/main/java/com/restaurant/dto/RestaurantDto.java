package com.restaurant.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import com.restaurant.dto.MenuDTO;
import com.restaurant.dto.ReviewDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDTO {
    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private String description;
    private String imageUrl;
    private String businessHours;
    private Integer maxCapacity;
    private String category;
    @Builder.Default
    private boolean open = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private Double rating = 0.0;
    @Builder.Default
    private List<MenuDTO> menus = new ArrayList<>();
    @Builder.Default
    private List<ReviewDTO> reviews = new ArrayList<>();
    @Builder.Default
    private Integer reservationInterval = 30;
} 