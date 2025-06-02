package com.restaurant.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private String phone;

    private String description;

    private String imageUrl;

    // 예약 관련 필드 추가
    private int maxCapacity; // 최대 수용 인원
    private String businessHours; // 영업 시간 정보 (예: "월-금 10:00-22:00")
    private Integer reservationInterval; // 예약 간격 (분 단위)

    @OneToOne
    @JoinColumn(name = "manager_id")
    private User manager; // 레스토랑 매니저 (User 엔티티와의 관계)

    // TODO: Add other relevant fields like operating hours, category, etc.

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();
} 