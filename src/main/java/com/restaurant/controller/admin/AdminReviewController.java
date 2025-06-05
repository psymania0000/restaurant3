package com.restaurant.controller.admin;

import com.restaurant.dto.ReviewDTO;
import com.restaurant.service.ReviewService;
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
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
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

            Long restaurantId = restaurantService.getRestaurantIdByAdminEmail(userDetails.getUsername());
            List<ReviewDTO> reviews = reviewService.getRestaurantReviews(restaurantId);
            
            model.addAttribute("reviews", reviews);
            return "admin/reviews/list";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/dashboard";
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

            Long restaurantId = restaurantService.getRestaurantIdByAdminEmail(userDetails.getUsername());
            ReviewDTO review = reviewService.getReviewById(id);
            
            if (!review.getRestaurantId().equals(restaurantId)) {
                throw new EntityNotFoundException("해당 리뷰를 찾을 수 없습니다.");
            }
            
            reviewService.deleteReview(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "리뷰가 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/admin/reviews";
    }
} 