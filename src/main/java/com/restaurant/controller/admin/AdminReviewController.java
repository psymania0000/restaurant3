package com.restaurant.controller.admin;

import com.restaurant.dto.ReviewDTO;
import com.restaurant.service.ReviewService;
import com.restaurant.service.RestaurantService;
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
import java.util.ArrayList;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminReviewController {

    private final ReviewService reviewService;
    private final RestaurantService restaurantService;

    @GetMapping
    public String listReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            if (userDetails == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
                return "redirect:/login";
            }

            List<ReviewDTO> reviews;
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
                // 통합관리자: 전체 리뷰
                reviews = reviewService.getAllReviews();
            } else {
                // 식당 관리자: 본인 식당(들) 리뷰
                List<Long> restaurantIds = restaurantService.getRestaurantIdsByEmail(userDetails.getUsername());
                if (restaurantIds.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "관리 중인 식당이 없습니다.");
                    return "redirect:/admin/dashboard";
                }
                reviews = new ArrayList<>();
                for (Long restaurantId : restaurantIds) {
                    reviews.addAll(reviewService.getRestaurantReviews(restaurantId));
                }
            }

            model.addAttribute("reviews", reviews);
            return "admin/reviews/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            if (userDetails == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
                return "redirect:/login";
            }

            reviewService.deleteReview(id, userDetails.getUsername(), userDetails.getAuthorities());
            redirectAttributes.addFlashAttribute("successMessage", "리뷰가 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    @GetMapping("/{id}")
    public String reviewDetail(@PathVariable Long id, Model model) {
        ReviewDTO review = reviewService.getReviewById(id);
        model.addAttribute("review", review);
        return "admin/reviews/detail";
    }
} 