package com.restaurant.controller.admin;

import com.restaurant.dto.UserDto;
import com.restaurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        // TODO: Fetch list of all users
        List<UserDto> users = userService.getAllUsers(); // Assuming getAllUsers() exists or will be created
        model.addAttribute("users", users);
        return "admin/users/list";
    }

    // 특정 사용자 상세 정보 조회
    @GetMapping("/{id}")
    public String viewUserDetail(@PathVariable Long id, Model model) {
        UserDto user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/users/detail";
    }

    // 사용자 정보 수정 폼 보여주기
    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        UserDto user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/users/edit";
    }

    // 사용자 정보 수정 처리
    @PostMapping("/{id}")
    public String updateUser(@PathVariable Long id, UserDto userDto, RedirectAttributes redirectAttributes) {
        userDto.setId(id); // Ensure the ID is set in the DTO
        try {
            userService.updateUser(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users"; // Redirect to list if user not found
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
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

    // 사용자 역할 업데이트 처리
    @PostMapping("/{id}/update-role")
    public String updateUserRole(@PathVariable Long id, @RequestParam("role") String role, RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserRole(id, role); // UserService에 updateUserRole 메서드가 필요
            redirectAttributes.addFlashAttribute("successMessage", "User role updated successfully.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user role: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }
} 