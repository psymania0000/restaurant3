package com.restaurant.dto;

import com.restaurant.entity.MenuCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private MenuCategory category;
    private Boolean available;
    private Long restaurantId;
    private String imageUrl;
}