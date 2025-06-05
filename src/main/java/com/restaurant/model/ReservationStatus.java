package com.restaurant.model;

public enum ReservationStatus {
    PENDING("대기중"),
    APPROVED("승인됨"),
    REJECTED("거절됨"),
    CANCELLED("취소됨"),
    COMPLETED("완료됨");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ReservationStatus fromString(String status) {
        for (ReservationStatus reservationStatus : ReservationStatus.values()) {
            if (reservationStatus.name().equalsIgnoreCase(status)) {
                return reservationStatus;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + status);
    }
} 