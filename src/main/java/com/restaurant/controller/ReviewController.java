package com.restaurant.controller;

import com.restaurant.dto.ReviewDto;
import com.restaurant.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public String addReview(ReviewDto reviewDto, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof com.restaurant.entity.User) {
            com.restaurant.entity.User currentUser = (com.restaurant.entity.User) authentication.getPrincipal();
            try {
                reviewService.createReview(reviewDto, currentUser.getId());
                redirectAttributes.addFlashAttribute("successMessage", "후기가 성공적으로 작성되었습니다.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "후기 작성 중 오류가 발생했습니다: " + e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 후 후기를 작성할 수 있습니다.");
        }

        // Redirect back to the restaurant detail page
        return "redirect:/restaurants/" + reviewDto.getRestaurantId();
    }
} 