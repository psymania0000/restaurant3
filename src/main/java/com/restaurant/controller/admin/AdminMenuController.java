package com.restaurant.controller.admin;

import com.restaurant.dto.MenuDTO;
import com.restaurant.service.MenuService;
import com.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Controller
@RequestMapping("/admin/menu-management")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;
    private final RestaurantService restaurantService;

    // 메뉴 목록 조회
    @GetMapping
    public String listMenus(@AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (userDetails == null) {
                redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
                return "redirect:/login";
            }

            Long restaurantId = restaurantService.getRestaurantIdByAdminEmail(userDetails.getUsername());
            List<MenuDTO> menus = menuService.getMenusByRestaurantId(restaurantId);
            model.addAttribute("menus", menus);
            model.addAttribute("restaurantId", restaurantId);
            return "admin/menus/list";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "메뉴 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @GetMapping("/{id}")
    public String getMenu(@PathVariable Long id, Model model) {
        model.addAttribute("menu", menuService.getMenuById(id));
        return "admin/menus/detail";
    }

    // 메뉴 추가 폼
    @GetMapping("/new")
    public String newMenuForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Long restaurantId = restaurantService.getRestaurantIdByAdminEmail(userDetails.getUsername());
        MenuDTO menuDTO = new MenuDTO();
        menuDTO.setRestaurantId(restaurantId);
        model.addAttribute("menu", menuDTO);
        return "admin/menus/form";
    }

    // 메뉴 추가 처리
    @PostMapping
    public String createMenu(@ModelAttribute MenuDTO menuDTO, @RequestParam(required = false) MultipartFile image, RedirectAttributes redirectAttributes) {
        try {
            menuService.createMenu(menuDTO, image);
            redirectAttributes.addFlashAttribute("message", "메뉴가 생성되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "메뉴 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/menu-management";
    }

    // 메뉴 수정 폼
    @GetMapping("/{id}/edit")
    public String editMenuForm(@PathVariable Long id, Model model) {
        model.addAttribute("menu", menuService.getMenuById(id));
        return "admin/menus/form";
    }

    // 메뉴 수정 처리
    @PostMapping("/{id}")
    public String updateMenu(@PathVariable Long id, @ModelAttribute MenuDTO menuDTO, @RequestParam(required = false) MultipartFile image, RedirectAttributes redirectAttributes) {
        try {
            menuService.updateMenu(id, menuDTO, image);
            redirectAttributes.addFlashAttribute("message", "메뉴가 업데이트되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "메뉴 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/menu-management";
    }

    // 메뉴 삭제 처리
    @PostMapping("/{id}/delete")
    public String deleteMenu(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            menuService.deleteMenu(id);
            redirectAttributes.addFlashAttribute("message", "메뉴가 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "메뉴 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/menu-management";
    }

    @PostMapping("/{id}/toggle")
    public String toggleMenuAvailability(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            menuService.toggleMenuAvailability(id);
            redirectAttributes.addFlashAttribute("message", "메뉴 상태가 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "메뉴 상태 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/menu-management";
    }
} 