package com.restaurant.controller;

import com.restaurant.dto.RestaurantDto;
import com.restaurant.dto.ReviewDto;
import com.restaurant.service.MenuService;
import com.restaurant.service.RestaurantService;
import com.restaurant.service.ReviewService;
import com.restaurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final ReviewService reviewService;
    private final UserService userService;

    @GetMapping("/restaurants/{id}")
    public String getRestaurantDetail(@PathVariable Long id, Model model) {
        RestaurantDto restaurant = restaurantService.getRestaurantById(id);
        model.addAttribute("restaurant", restaurant);

        // 메뉴 목록 추가
        List<com.restaurant.dto.MenuDto> menus = menuService.getRestaurantMenus(id);
        model.addAttribute("menus", menus);

        // 후기 목록 추가
        List<ReviewDto> reviews = reviewService.getReviewsByRestaurantId(id);
        model.addAttribute("reviews", reviews);

        // 사용자의 현재 포인트 정보 추가
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof com.restaurant.entity.User) {
            com.restaurant.entity.User currentUser = (com.restaurant.entity.User) authentication.getPrincipal();
            model.addAttribute("userPoints", currentUser.getPoints());
        } else {
            model.addAttribute("userPoints", 0); // 비로그인 시 0 포인트
        }

        return "restaurant/detail";
    }
} 