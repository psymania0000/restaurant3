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

@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminDashboardController {

    private final RestaurantService restaurantService;
    private final UserService userService;
    private final MenuService menuService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            Long restaurantId = restaurantService.getRestaurantIdByAdminEmail(userDetails.getUsername());
            model.addAttribute("restaurantId", restaurantId);
        } catch (Exception e) {
            // 식당이 없는 경우 restaurantId는 null로 유지
        }

        model.addAttribute("userCount", userService.getUserCount());
        model.addAttribute("restaurantCount", restaurantService.getRestaurantCount());
        model.addAttribute("menuCount", menuService.getMenuCount());
        model.addAttribute("reservationCount", reservationService.getReservationCount());
        model.addAttribute("reviewCount", reviewService.getReviewCount());

        return "admin/dashboard";
    }
} 