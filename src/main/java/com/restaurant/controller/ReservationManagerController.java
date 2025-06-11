package com.restaurant.controller;

import com.restaurant.dto.ReservationDTO;
import com.restaurant.dto.RestaurantDTO;
import com.restaurant.service.ReservationService;
import com.restaurant.service.RestaurantService;
import com.restaurant.model.ReservationStatus;
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
        
        RestaurantDTO restaurant = restaurantService.getRestaurantByEmail(userDetails.getUsername());
        List<ReservationDTO> reservations;
        
        if (status != null) {
            try {
                ReservationStatus reservationStatus = ReservationStatus.valueOf(status.toUpperCase());
                reservations = reservationService.getReservationsByRestaurantIdAndStatus(restaurant.getId(), reservationStatus);
            } catch (IllegalArgumentException e) {
                reservations = reservationService.getReservationsByRestaurantId(restaurant.getId());
            }
        } else {
            reservations = reservationService.getReservationsByRestaurantId(restaurant.getId());
        }
        
        model.addAttribute("reservations", reservations);
        model.addAttribute("status", status);
        return "manager/reservations/list";
    }

    // 예약 승인
    @PostMapping("/{id}/confirm")
    public String confirmReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        try {
            RestaurantDTO restaurant = restaurantService.getRestaurantByEmail(userDetails.getUsername());
            ReservationDTO reservation = reservationService.getReservationById(id);
            
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
            RestaurantDTO restaurant = restaurantService.getRestaurantByEmail(userDetails.getUsername());
            ReservationDTO reservation = reservationService.getReservationById(id);
            
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

    @PostMapping("/{id}/status")
    public String updateReservationStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {
        try {
            ReservationStatus reservationStatus = ReservationStatus.valueOf(status.toUpperCase());
            reservationService.updateReservationStatus(id, reservationStatus);
            redirectAttributes.addFlashAttribute("successMessage", "예약 상태가 업데이트되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "잘못된 예약 상태입니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/manager/reservations";
    }

    @GetMapping("/pending")
    public String listPendingReservations(Model model) {
        model.addAttribute("reservations", reservationService.getReservationsByStatus(ReservationStatus.PENDING));
        return "manager/reservations/pending";
    }

    @GetMapping("/approved")
    public String listApprovedReservations(Model model) {
        model.addAttribute("reservations", reservationService.getReservationsByStatus(ReservationStatus.APPROVED));
        return "manager/reservations/approved";
    }
} 