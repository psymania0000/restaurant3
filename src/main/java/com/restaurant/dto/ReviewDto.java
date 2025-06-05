package com.restaurant.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private String content;
    private Integer rating;
    private Long restaurantId;
    private String restaurantName;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 