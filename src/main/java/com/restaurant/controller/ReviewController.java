package com.restaurant.controller;

import com.restaurant.dto.ReviewDTO;
import com.restaurant.service.ReviewService;
import com.restaurant.service.RestaurantService;
import com.restaurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final RestaurantService restaurantService;
    private final UserService userService;

    @GetMapping("/{id}")
    public String getReviewDetail(@PathVariable Long id, Model model) {
        ReviewDTO review = reviewService.getReviewById(id);
        model.addAttribute("review", review);
        return "review/detail";
    }

    @PostMapping
    public String createReview(@ModelAttribute ReviewDTO reviewDTO, 
                             @RequestParam Long restaurantId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        reviewService.createReview(reviewDTO, restaurantId, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("message", "리뷰가 등록되었습니다.");
        return "redirect:/restaurants/" + restaurantId;
    }

    @GetMapping("/user")
    public String getUserReviews(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<ReviewDTO> reviews = reviewService.getReviewsByUsername(userDetails.getUsername());
        model.addAttribute("reviews", reviews);
        return "review/user-reviews";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        ReviewDTO review = reviewService.getReviewById(id);
        if (!review.getAuthor().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("리뷰를 수정할 권한이 없습니다.");
        }
        model.addAttribute("review", review);
        return "review/edit";
    }

    @PostMapping("/{id}")
    public String updateReview(@PathVariable Long id, 
                             @ModelAttribute ReviewDTO reviewDTO,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        reviewService.updateReview(id, reviewDTO, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("message", "리뷰가 수정되었습니다.");
        return "redirect:/reviews/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        ReviewDTO review = reviewService.getReviewById(id);
        reviewService.deleteReview(id, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("message", "리뷰가 삭제되었습니다.");
        return "redirect:/restaurants/" + review.getRestaurantId();
    }
} 