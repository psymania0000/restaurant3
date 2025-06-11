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
import org.springframework.security.access.prepost.PreAuthorize;
import com.restaurant.entity.Restaurant;
import com.restaurant.service.RestaurantService;
import com.restaurant.entity.User;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final RestaurantService restaurantService;

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
    public String updateUser(@PathVariable Long id, @Valid @ModelAttribute UserDTO userDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/users/" + id + "/edit";
        }
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

    @PostMapping("/{id}/update-role")
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

    // 관리자 추가 폼
    @GetMapping("/new")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public String newAdminForm(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        model.addAttribute("restaurants", restaurantService.getAllRestaurants());
        return "admin/users/new";
    }

    // 관리자 등록 처리
    @PostMapping("/new")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public String createAdmin(@ModelAttribute UserDTO userDTO, @RequestParam(value = "restaurantIds", required = false) Long[] restaurantIds, RedirectAttributes redirectAttributes) {
        if (userService.existsByUsername(userDTO.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 사용 중인 아이디입니다.");
            return "redirect:/admin/users/new";
        }
        if (userService.existsByEmail(userDTO.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 사용 중인 이메일입니다.");
            return "redirect:/admin/users/new";
        }
        if (userService.existsByPhone(userDTO.getPhone())) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 사용 중인 전화번호입니다.");
            return "redirect:/admin/users/new";
        }
        userDTO.setRole(UserRole.ADMIN); // 반드시 ROLE_ADMIN으로 고정
        User user = userService.createUserEntity(userDTO); // 엔티티 반환용 메서드 필요
        if (restaurantIds != null && restaurantIds.length > 0) {
            List<Restaurant> managedRestaurants = new ArrayList<>();
            for (Long rid : restaurantIds) {
                Restaurant restaurant = restaurantService.getRestaurantEntityById(rid);
                managedRestaurants.add(restaurant);
            }
            user.setManagedRestaurants(new HashSet<>(managedRestaurants));
        }
        userService.saveUser(user);
        redirectAttributes.addFlashAttribute("message", "관리자가 추가되었습니다.");
        return "redirect:/admin/users";
    }
} 