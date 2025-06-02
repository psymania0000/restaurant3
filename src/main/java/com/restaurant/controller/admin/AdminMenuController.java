package com.restaurant.controller.admin;

import com.restaurant.dto.MenuDto;
import com.restaurant.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/admin/menus")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;

    // 메뉴 목록 조회
    @GetMapping
    public String listMenus(Model model) {
        // TODO: Fetch menus for a specific restaurant (requires authentication)
        // For now, fetching all menus as a placeholder
        model.addAttribute("menus", menuService.getAllMenus());
        return "admin/menu/list";
    }

    // 메뉴 추가 폼
    @GetMapping("/new")
    public String newMenuForm(Model model) {
        model.addAttribute("menu", new MenuDto());
        // TODO: Pass restaurantId to associate menu with a restaurant
        return "admin/menu/form";
    }

    // 메뉴 추가 처리
    @PostMapping
    public String createMenu(@ModelAttribute MenuDto menuDto, @RequestParam("imageFile") MultipartFile imageFile, RedirectAttributes redirectAttributes) {
        if (!imageFile.isEmpty()) {
            try {
                String uploadDir = "src/main/resources/static/images/menus"; // Specify your upload directory
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = Paths.get(uploadDir, uniqueFileName);
                Files.copy(imageFile.getInputStream(), filePath);
                menuDto.setImageUrl("/images/menus/" + uniqueFileName);
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("message", "Error uploading image: " + e.getMessage());
                return "redirect:/admin/menus/new";
            }
        }
        // TODO: Set restaurantId for the menu
        menuService.createMenu(menuDto);
        redirectAttributes.addFlashAttribute("message", "메뉴가 성공적으로 추가되었습니다.");
        return "redirect:/admin/menus";
    }

    // 메뉴 수정 폼
    @GetMapping("/{id}/edit")
    public String editMenuForm(@PathVariable Long id, Model model) {
        // TODO: Fetch menu by id and pass to form (requires MenuService method)
        // model.addAttribute("menu", menuService.getMenuById(id));
        model.addAttribute("menu", new MenuDto()); // Placeholder
        return "admin/menu/form";
    }

    // 메뉴 수정 처리
    @PostMapping("/{id}")
    public String updateMenu(@PathVariable Long id, @ModelAttribute MenuDto menuDto, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile, RedirectAttributes redirectAttributes) {
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String uploadDir = "src/main/resources/static/images/menus"; // Specify your upload directory
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = Paths.get(uploadDir, uniqueFileName);
                Files.copy(imageFile.getInputStream(), filePath);
                menuDto.setImageUrl("/images/menus/" + uniqueFileName);
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("message", "Error uploading image: " + e.getMessage());
                return "redirect:/admin/menus/" + id + "/edit";
            }
        } else {
             // If no new image is uploaded, keep the existing image URL
             // This requires fetching the existing menu first
             // menuDto.setImageUrl(menuService.getMenuById(id).getImageUrl());
        }
        menuService.updateMenu(id, menuDto);
        redirectAttributes.addFlashAttribute("message", "메뉴가 성공적으로 수정되었습니다.");
        return "redirect:/admin/menus";
    }

    // 메뉴 삭제 처리
    @PostMapping("/{id}/delete")
    public String deleteMenu(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        menuService.deleteMenu(id);
        redirectAttributes.addFlashAttribute("message", "메뉴가 성공적으로 삭제되었습니다.");
        return "redirect:/admin/menus";
    }
} 