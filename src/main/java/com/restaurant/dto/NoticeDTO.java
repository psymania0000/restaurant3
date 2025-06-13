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
public class NoticeDTO {
    private Long id;
    private String title;
    private String content;
    private boolean important;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByName;
    private Long createdById;
} 