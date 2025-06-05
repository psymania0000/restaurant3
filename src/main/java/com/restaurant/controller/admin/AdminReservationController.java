package com.restaurant.controller.admin;

import com.restaurant.dto.ReservationDTO;
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
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;
    private final RestaurantService restaurantService;

    @GetMapping
    public String listReservations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (userDetails == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
                return "redirect:/login";
            }

            Long restaurantId = restaurantService.getRestaurantIdByAdminEmail(userDetails.getUsername());
            List<ReservationDTO> reservations;
            
            if (status != null) {
                try {
                    ReservationStatus reservationStatus = ReservationStatus.valueOf(status.toUpperCase());
                    reservations = reservationService.getReservationsByRestaurantIdAndStatus(restaurantId, reservationStatus);
                } catch (IllegalArgumentException e) {
                    reservations = reservationService.getReservationsByRestaurantId(restaurantId);
                }
            } else {
                reservations = reservationService.getReservationsByRestaurantId(restaurantId);
            }
            
            model.addAttribute("reservations", reservations);
            model.addAttribute("status", status);
            return "admin/reservations/list";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/{id}/confirm")
    public String confirmReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (userDetails == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
                return "redirect:/login";
            }

            Long restaurantId = restaurantService.getRestaurantIdByAdminEmail(userDetails.getUsername());
            ReservationDTO reservation = reservationService.getReservationById(id);
            
            if (!reservation.getRestaurantId().equals(restaurantId)) {
                throw new EntityNotFoundException("해당 예약을 찾을 수 없습니다.");
            }
            
            reservationService.confirmReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "예약이 승인되었습니다.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 승인 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/admin/reservations";
    }

    @PostMapping("/{id}/cancel")
    public String cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (userDetails == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
                return "redirect:/login";
            }

            Long restaurantId = restaurantService.getRestaurantIdByAdminEmail(userDetails.getUsername());
            ReservationDTO reservation = reservationService.getReservationById(id);
            
            if (!reservation.getRestaurantId().equals(restaurantId)) {
                throw new EntityNotFoundException("해당 예약을 찾을 수 없습니다.");
            }
            
            reservationService.cancelReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "예약이 취소되었습니다.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 취소 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/admin/reservations";
    }

    @PostMapping("/{id}/status")
    public String updateReservationStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            if (userDetails == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
                return "redirect:/login";
            }

            Long restaurantId = restaurantService.getRestaurantIdByAdminEmail(userDetails.getUsername());
            ReservationDTO reservation = reservationService.getReservationById(id);
            
            if (!reservation.getRestaurantId().equals(restaurantId)) {
                throw new EntityNotFoundException("해당 예약을 찾을 수 없습니다.");
            }

            ReservationStatus reservationStatus = ReservationStatus.valueOf(status.toUpperCase());
            reservationService.updateReservationStatus(id, reservationStatus);
            redirectAttributes.addFlashAttribute("successMessage", "예약 상태가 업데이트되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "잘못된 예약 상태입니다.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/reservations";
    }
} 