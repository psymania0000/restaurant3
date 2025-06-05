package com.restaurant.dto;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import com.restaurant.dto.MenuDTO;
import com.restaurant.dto.ReviewDTO;

@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDTO {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String description;
    private String imageUrl;
    private String businessHours;
    private Integer maxCapacity;
    private String category;
    private boolean open;
    private String adminEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double rating;
    private List<MenuDTO> menus;
    private List<ReviewDTO> reviews;
    private Integer reservationInterval;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getBusinessHours() { return businessHours; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public String getCategory() { return category; }
    public boolean isOpen() { return open; }
    public String getAdminEmail() { return adminEmail; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Double getRating() { return rating; }
    public List<MenuDTO> getMenus() { return menus; }
    public List<ReviewDTO> getReviews() { return reviews; }
    public Integer getReservationInterval() { return reservationInterval; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setBusinessHours(String businessHours) { this.businessHours = businessHours; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
    public void setCategory(String category) { this.category = category; }
    public void setOpen(boolean open) { this.open = open; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setRating(Double rating) { this.rating = rating; }
    public void setMenus(List<MenuDTO> menus) { this.menus = menus; }
    public void setReviews(List<ReviewDTO> reviews) { this.reviews = reviews; }
    public void setReservationInterval(Integer reservationInterval) { this.reservationInterval = reservationInterval; }
} 