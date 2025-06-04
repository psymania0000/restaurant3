package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private Long restaurantId;
    private Long userId;
    private String userName;
    private String comment;
    private int rating;
    private LocalDateTime createdAt;
    // TODO: Add other fields like updatedAt if needed
} 