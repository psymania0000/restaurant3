package com.restaurant.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
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