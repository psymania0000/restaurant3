package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.restaurant.model.ReservationStatus;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    private Long id;
    private Long userId;
    private Long restaurantId;
    private LocalDateTime reservationTime;
    private Integer numberOfPeople;
    private ReservationStatus status;
    private String specialRequests;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String restaurantName;
    private Integer pointsToUse;
    private String request;
}