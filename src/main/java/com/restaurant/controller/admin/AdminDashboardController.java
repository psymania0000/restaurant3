package com.restaurant.controller.admin;

import com.restaurant.service.RestaurantService;
import com.restaurant.service.UserService;
import com.restaurant.service.MenuService;
import com.restaurant.service.ReservationService;
import com.restaurant.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);
    private final RestaurantService restaurantService;
    private final UserService userService;
    private final MenuService menuService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;

    @GetMapping
    public String redirectToDashboard() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            if (userDetails == null) {
                logger.warn("UserDetails is null");
                return "redirect:/login";
            }

            boolean isSuperAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
            model.addAttribute("isSuperAdmin", isSuperAdmin);

            if (!isSuperAdmin) {
                try {
                    List<Long> restaurantIds = restaurantService.getRestaurantIdsByEmail(userDetails.getUsername());
                    if (restaurantIds == null || restaurantIds.isEmpty()) {
                        model.addAttribute("errorMessage", "관리 중인 식당이 없습니다.");
                    } else {
                        model.addAttribute("restaurantIds", restaurantIds);
                    }
                } catch (EntityNotFoundException e) {
                    logger.warn("No restaurant found for user: {}", userDetails.getUsername());
                    model.addAttribute("errorMessage", "관리 중인 식당이 없습니다.");
                }
            }

            // 통계 정보 추가
            try {
                model.addAttribute("userCount", userService.getUserCount());
                model.addAttribute("restaurantCount", restaurantService.getRestaurantCount());
                model.addAttribute("menuCount", menuService.getMenuCount());
                model.addAttribute("reservationCount", reservationService.getReservationCount());
                model.addAttribute("reviewCount", reviewService.getReviewCount());
            } catch (Exception e) {
                logger.error("Error while fetching statistics: {}", e.getMessage());
                model.addAttribute("errorMessage", "통계 정보를 불러오는 중 오류가 발생했습니다.");
            }

            return "admin/dashboard";
        } catch (Exception e) {
            logger.error("Unexpected error in dashboard: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "대시보드 로딩 중 오류가 발생했습니다.");
            return "admin/dashboard";
        }
    }
} 