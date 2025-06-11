package com.restaurant.controller;

import com.restaurant.dto.LoginRequest;
import com.restaurant.dto.SignupRequest;
import com.restaurant.dto.UserDTO;
import com.restaurant.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public String loginPage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, 
                       @RequestParam String password,
                       @RequestParam(required = false) String rememberMe,
                       HttpServletRequest request,
                       RedirectAttributes redirectAttributes) {
        log.info("로그인 시도: username={}", username);
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);
            
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
            
            log.info("로그인 성공: username={}, authorities={}", 
                    authentication.getName(), 
                    authentication.getAuthorities());
            
            return "redirect:/";
        } catch (Exception e) {
            log.error("로그인 실패: username={}, error={}", username, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "redirect:/login";
        }
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupRequest signupRequest, 
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        log.info("회원가입 시도: username={}, email={}", signupRequest.getUsername(), signupRequest.getEmail());
        
        try {
            if (bindingResult.hasErrors()) {
                log.warn("회원가입 유효성 검사 실패: {}", bindingResult.getAllErrors());
                redirectAttributes.addFlashAttribute("error", "입력 정보를 확인해주세요.");
                return "redirect:/signup";
            }

            // 아이디 중복 체크
            if (userService.existsByUsername(signupRequest.getUsername())) {
                log.warn("회원가입 실패: 이미 존재하는 아이디 - {}", signupRequest.getUsername());
                redirectAttributes.addFlashAttribute("error", "이미 사용 중인 아이디입니다.");
                return "redirect:/signup";
            }

            // 이메일 중복 체크
            if (userService.existsByEmail(signupRequest.getEmail())) {
                log.warn("회원가입 실패: 이미 존재하는 이메일 - {}", signupRequest.getEmail());
                redirectAttributes.addFlashAttribute("error", "이미 사용 중인 이메일입니다.");
                return "redirect:/signup";
            }

            // 전화번호 중복 체크
            if (userService.existsByPhone(signupRequest.getPhone())) {
                log.warn("회원가입 실패: 이미 존재하는 전화번호 - {}", signupRequest.getPhone());
                redirectAttributes.addFlashAttribute("error", "이미 사용 중인 전화번호입니다.");
                return "redirect:/signup";
            }

            log.info("회원가입 정보 검증 완료, 사용자 생성 시작");
            
            // 회원가입 처리
            userService.signup(signupRequest);
            
            log.info("회원가입 성공: username={}", signupRequest.getUsername());
            redirectAttributes.addFlashAttribute("success", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/login";
            
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "회원가입 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:/signup";
        }
    }

    @GetMapping("/api/check-username")
    @ResponseBody
    public Map<String, Boolean> checkUsername(@RequestParam String username) {
        log.info("아이디 중복 체크: {}", username);
        boolean available = !userService.existsByUsername(username);
        return Map.of("available", available);
    }
} 