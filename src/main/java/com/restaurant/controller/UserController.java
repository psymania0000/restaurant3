package com.restaurant.controller;

import com.restaurant.dto.SignupRequest;
import com.restaurant.dto.UserDTO;
import com.restaurant.entity.User;
import com.restaurant.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import jakarta.validation.Valid;

@Slf4j
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "auth/signup";
    }

    @GetMapping("/check-username")
    @ResponseBody
    public Map<String, Boolean> checkUsername(@RequestParam String username) {
        boolean available = !userService.existsByUsername(username);
        return Map.of("available", available);
    }

    @GetMapping("/check-phone")
    @ResponseBody
    public Map<String, Boolean> checkPhone(@RequestParam String phone) {
        boolean available = !userService.existsByPhone(phone);
        return Map.of("available", available);
    }

    @PostMapping("/signup")
    public String registerUser(@Valid @ModelAttribute("signupRequest") SignupRequest signupRequest, 
                             BindingResult result, 
                             RedirectAttributes redirectAttributes) {
        log.info("회원가입 요청 시작: {}", signupRequest.getUsername());
        
        if (result.hasErrors()) {
            log.error("유효성 검사 오류: {}", result.getAllErrors());
            result.getAllErrors().forEach(error -> {
                redirectAttributes.addFlashAttribute("errorMessage", error.getDefaultMessage());
            });
            return "redirect:/users/signup";
        }

        if (!signupRequest.getPassword().equals(signupRequest.getPasswordConfirm())) {
            log.error("비밀번호 불일치");
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");
            return "redirect:/users/signup";
        }

        try {
            log.info("회원가입 처리 시작");
            User savedUser = userService.signup(signupRequest);
            if (savedUser != null) {
                log.info("회원가입 성공: {}", savedUser.getUsername());
                redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다. 로그인해주세요.");
                return "redirect:/login";
            } else {
                log.error("회원가입 실패: 저장된 사용자 정보 없음");
                redirectAttributes.addFlashAttribute("errorMessage", "회원가입 처리 중 오류가 발생했습니다.");
                return "redirect:/users/signup";
            }
        } catch (RuntimeException e) {
            log.error("회원가입 중 예외 발생: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users/signup";
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

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/lock")
    public ResponseEntity<UserDTO> lockUser(@PathVariable Long userId) {
        UserDTO lockedUser = userService.lockUser(userId);
        return ResponseEntity.ok(lockedUser);
    }

    @PostMapping("/{userId}/unlock")
    public ResponseEntity<UserDTO> unlockUser(@PathVariable Long userId) {
        UserDTO unlockedUser = userService.unlockUser(userId);
        return ResponseEntity.ok(unlockedUser);
    }

    @DeleteMapping("/{userId}/withdraw")
    public ResponseEntity<Void> withdrawUser(@PathVariable Long userId) {
        userService.withdrawUser(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserDTO> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile")
    public String getProfile(@RequestParam Long userId, Model model) {
        UserDTO user = userService.getUserById(userId);
        model.addAttribute("user", user);
        return "user/profile";
    }

    @GetMapping("/edit")
    public String editForm(@RequestParam Long userId, Model model) {
        UserDTO user = userService.getUserById(userId);
        model.addAttribute("user", user);
        return "user/edit";
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute UserDTO userDTO, RedirectAttributes redirectAttributes) {
        UserDTO updatedUser = userService.updateUser(userDTO.getId(), userDTO);
        redirectAttributes.addFlashAttribute("message", "사용자 정보가 업데이트되었습니다.");
        return "redirect:/users/profile?userId=" + updatedUser.getId();
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam Long userId,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {
        UserDTO updatedUser = userService.changePassword(userId, currentPassword, newPassword);
        redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
        return "redirect:/users/profile?userId=" + updatedUser.getId();
    }
} 