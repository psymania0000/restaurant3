package com.restaurant.controller;

import com.restaurant.dto.ReservationDTO;
import com.restaurant.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final ReservationService reservationService;

    @GetMapping
    public String myPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof com.restaurant.entity.User) {
            com.restaurant.entity.User currentUser = (com.restaurant.entity.User) authentication.getPrincipal();
            List<ReservationDTO> reservations = reservationService.getUserReservations(currentUser.getId());
            model.addAttribute("reservations", reservations);
            model.addAttribute("user", currentUser);
            return "mypage/index";
        }
        return "redirect:/login";
    }
} 