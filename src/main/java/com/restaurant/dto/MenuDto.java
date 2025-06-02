package com.restaurant.dto;

import lombok.Data;

@Data
public class MenuDto {
    private Long id;
    private Long restaurantId;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String imageUrl;
} 