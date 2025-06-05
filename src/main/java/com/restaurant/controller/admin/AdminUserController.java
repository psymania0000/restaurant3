package com.restaurant.controller.admin;

import com.restaurant.dto.UserDTO;
import com.restaurant.model.UserRole;
import com.restaurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users/list";
    }

    @GetMapping("/{id}")
    public String getUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "admin/users/detail";
    }

    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("roles", UserRole.values());
        return "admin/users/edit";
    }

    @PostMapping("/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute UserDTO userDTO, RedirectAttributes redirectAttributes) {
        userService.updateUser(id, userDTO);
        redirectAttributes.addFlashAttribute("message", "사용자 정보가 업데이트되었습니다.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/lock")
    public String lockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.lockUser(id);
        redirectAttributes.addFlashAttribute("message", "사용자가 잠겼습니다.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/unlock")
    public String unlockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.unlockUser(id);
        redirectAttributes.addFlashAttribute("message", "사용자 잠금이 해제되었습니다.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/role")
    public String updateUserRole(@PathVariable Long id, @RequestParam String role, RedirectAttributes redirectAttributes) {
        UserDTO userDTO = userService.getUserById(id);
        userDTO.setRole(UserRole.fromString(role));
        userService.updateUser(id, userDTO);
        redirectAttributes.addFlashAttribute("message", "사용자 역할이 업데이트되었습니다.");
        return "redirect:/admin/users";
    }

    // 사용자 삭제 처리
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
} 