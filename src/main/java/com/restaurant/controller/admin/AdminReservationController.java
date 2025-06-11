package com.restaurant.controller.admin;

import com.restaurant.dto.ReservationDTO;
import com.restaurant.service.ReservationService;
import com.restaurant.service.RestaurantService;
import com.restaurant.model.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Controller
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
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

            List<ReservationDTO> reservations;
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
                // 통합관리자: 전체 예약
                reservations = reservationService.getAllReservations();
            } else {
                // 식당 관리자: 본인 식당(들) 예약
                List<Long> restaurantIds = restaurantService.getRestaurantIdsByEmail(userDetails.getUsername());
                if (restaurantIds.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "관리 중인 식당이 없습니다.");
                    return "redirect:/admin/dashboard";
                }
                reservations = reservationService.getReservationsByRestaurantIds(restaurantIds);
            }

            model.addAttribute("reservations", reservations);
            model.addAttribute("status", status);
            return "admin/reservations/list";
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

            boolean isSuperAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

            if (!isSuperAdmin) {
                // Regular admin: check restaurant ownership
                Long restaurantId = restaurantService.getRestaurantIdByEmail(userDetails.getUsername());
                ReservationDTO reservation = reservationService.getReservationById(id);
                
                if (!reservation.getRestaurantId().equals(restaurantId)) {
                    throw new EntityNotFoundException("해당 예약을 찾을 수 없습니다.");
                }
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

            boolean isSuperAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

            if (!isSuperAdmin) {
                // Regular admin: check restaurant ownership
                Long restaurantId = restaurantService.getRestaurantIdByEmail(userDetails.getUsername());
                ReservationDTO reservation = reservationService.getReservationById(id);
                
                if (!reservation.getRestaurantId().equals(restaurantId)) {
                    throw new EntityNotFoundException("해당 예약을 찾을 수 없습니다.");
                }
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

            boolean isSuperAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

            if (!isSuperAdmin) {
                // Regular admin: check restaurant ownership
                Long restaurantId = restaurantService.getRestaurantIdByEmail(userDetails.getUsername());
                ReservationDTO reservation = reservationService.getReservationById(id);
                
                if (!reservation.getRestaurantId().equals(restaurantId)) {
                    throw new EntityNotFoundException("해당 예약을 찾을 수 없습니다.");
                }
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