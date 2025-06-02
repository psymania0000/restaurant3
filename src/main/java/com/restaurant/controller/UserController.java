package com.restaurant.controller;

import com.restaurant.dto.SignupRequest;
import com.restaurant.dto.UserDto;
import com.restaurant.entity.User;
import com.restaurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup") // 회원가입 처리
    public String registerUser(@Valid @ModelAttribute("signupRequest") SignupRequest signupRequest, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // 유효성 검사 실패 시 회원가입 페이지로 돌아감
            return "signup"; // signup.html 뷰 이름
        }

        try {
            userService.signup(signupRequest); // UserService의 signup 메서드 호출
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 성공적으로 완료되었습니다. 로그인해주세요.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/signup";
        }
    }

    // TODO: 전화번호 인증 관련 엔드포인트 추가 (필요시)
    // @GetMapping("/verify/phone")
    // public String verifyPhone(@RequestParam String token, RedirectAttributes redirectAttributes) {
    //     try {
    //         userService.verifyPhone(token); // UserService의 verifyPhone 메서드 호출
    //         redirectAttributes.addFlashAttribute("successMessage", "전화번호 인증이 완료되었습니다.");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("errorMessage", "전화번호 인증에 실패했습니다: " + e.getMessage());
    //     }
    //     return "redirect:/login"; // 인증 후 로그인 페이지로 리다이렉트
    // }

    // TODO: 포인트 추가 엔드포인트 (관리자용 등) 추가 (필요시)
    // @PostMapping("/{userId}/points/add")
    // public String addPoints(@PathVariable Long userId, @RequestParam int points, RedirectAttributes redirectAttributes) {
    //     try {
    //         userService.addPoints(userId, points); // UserService의 addPoints 메서드 호출
    //         redirectAttributes.addFlashAttribute("successMessage", userId + " 사용자에게 " + points + " 포인트가 지급되었습니다.");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("errorMessage", "포인트 지급 중 오류가 발생했습니다: " + e.getMessage());
    //     }
    //      return "redirect:/admin/users/" + userId; // 사용자 상세 페이지로 리다이렉트
    // }

    // UserDto를 사용하는 임시 registerUser 메서드 (SignupRequest 사용으로 변경됨)
    // 빌드 오류 해결을 위해 남겨두거나 삭제 필요. 현재는 SignupRequest 사용하므로 이 메서드는 불필요
    // @PostMapping("/register")
    // public String registerUser(@Valid @ModelAttribute("userDto") UserDto userDto, BindingResult result, RedirectAttributes redirectAttributes) {
    //     if (result.hasErrors()) {
    //         return "signup";
    //     }
    //     try {
    //         userService.registerUser(userDto); // UserService의 registerUser 메서드 호출
    //         redirectAttributes.addFlashAttribute("successMessage", "회원가입이 성공적으로 완료되었습니다. 로그인해주세요.");
    //         return "redirect:/login";
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("errorMessage", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
    //         return "redirect:/signup";
    //     }
    // }
} 