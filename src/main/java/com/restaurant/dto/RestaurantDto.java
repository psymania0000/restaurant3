package com.restaurant.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantDto {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String description;
    private String imageUrl;
    // TODO: Add other fields as needed
} 