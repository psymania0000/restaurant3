package com.restaurant.controller;

import com.restaurant.dto.LoginRequest;
import com.restaurant.dto.SignupRequest;
import com.restaurant.dto.UserDto;
import com.restaurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "auth/signup";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @PostMapping("/api/auth/signup")
    public String signup(@ModelAttribute SignupRequest signupRequest) {
        userService.signup(signupRequest);
        return "redirect:/login";
    }

    @PostMapping("/api/auth/login")
    public String login(@ModelAttribute LoginRequest loginRequest) {
        UserDto userDto = userService.login(loginRequest);
        // TODO: JWT 토큰 생성 및 저장
        return "redirect:/";
    }
} 