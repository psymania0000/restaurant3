package com.restaurant.controller;

import com.restaurant.dto.ReservationDto;
import com.restaurant.dto.RestaurantDto;
import com.restaurant.service.ReservationService;
import com.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Controller
@RequestMapping("/manager/reservations")
@RequiredArgsConstructor
public class ReservationManagerController {

    private final ReservationService reservationService;
    private final RestaurantService restaurantService;

    // 예약 목록 조회
    @GetMapping
    public String listReservations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status,
            Model model) {
        
        RestaurantDto restaurant = restaurantService.getRestaurantByManagerEmail(userDetails.getUsername());
        List<ReservationDto> reservations;
        
        if (status != null) {
            reservations = reservationService.getReservationsByRestaurantIdAndStatus(restaurant.getId(), status);
        } else {
            reservations = reservationService.getReservationsByRestaurantId(restaurant.getId());
        }
        
        model.addAttribute("reservations", reservations);
        model.addAttribute("status", status);
        return "manager/reservations";
    }

    // 예약 승인
    @PostMapping("/{id}/confirm")
    public String confirmReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        try {
            RestaurantDto restaurant = restaurantService.getRestaurantByManagerEmail(userDetails.getUsername());
            ReservationDto reservation = reservationService.getReservationById(id);
            
            // 예약이 해당 레스토랑의 것인지 확인
            if (!reservation.getRestaurantId().equals(restaurant.getId())) {
                throw new EntityNotFoundException("Reservation not found");
            }
            
            reservationService.confirmReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "예약이 승인되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 승인 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/manager/reservations";
    }

    // 예약 거절
    @PostMapping("/{id}/cancel")
    public String cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        try {
            RestaurantDto restaurant = restaurantService.getRestaurantByManagerEmail(userDetails.getUsername());
            ReservationDto reservation = reservationService.getReservationById(id);
            
            // 예약이 해당 레스토랑의 것인지 확인
            if (!reservation.getRestaurantId().equals(restaurant.getId())) {
                throw new EntityNotFoundException("Reservation not found");
            }
            
            reservationService.cancelReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "예약이 거절되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 거절 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/manager/reservations";
    }
} 