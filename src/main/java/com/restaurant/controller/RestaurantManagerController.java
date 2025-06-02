package com.restaurant.controller;

import com.restaurant.dto.RestaurantDto;
import com.restaurant.dto.MenuDto;
import com.restaurant.service.RestaurantService;
import com.restaurant.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/manager/restaurant")
@RequiredArgsConstructor
public class RestaurantManagerController {

    private final RestaurantService restaurantService;
    private final MenuService menuService;

    // 레스토랑 관리자 대시보드
    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        RestaurantDto restaurant = restaurantService.getRestaurantByManagerEmail(userDetails.getUsername());
        model.addAttribute("restaurant", restaurant);
        return "manager/dashboard";
    }

    // 레스토랑 정보 수정 폼
    @GetMapping("/edit")
    public String editForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        RestaurantDto restaurant = restaurantService.getRestaurantByManagerEmail(userDetails.getUsername());
        model.addAttribute("restaurant", restaurant);
        return "manager/restaurant/edit";
    }

    // 레스토랑 정보 수정 처리
    @PostMapping("/edit")
    public String updateRestaurant(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute RestaurantDto restaurantDto,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {
        
        try {
            RestaurantDto currentRestaurant = restaurantService.getRestaurantByManagerEmail(userDetails.getUsername());
            restaurantDto.setId(currentRestaurant.getId());

            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = Paths.get("uploads", uniqueFileName);
                Files.copy(imageFile.getInputStream(), filePath);
                restaurantDto.setImageUrl("/uploaded_images/" + uniqueFileName);
            } else {
                restaurantDto.setImageUrl(currentRestaurant.getImageUrl());
            }

            restaurantService.updateRestaurant(currentRestaurant.getId(), restaurantDto);
            redirectAttributes.addFlashAttribute("successMessage", "레스토랑 정보가 성공적으로 수정되었습니다.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "레스토랑 정보 수정 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/manager/restaurant";
    }

    // 메뉴 관리 페이지
    @GetMapping("/menus")
    public String manageMenus(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        RestaurantDto restaurant = restaurantService.getRestaurantByManagerEmail(userDetails.getUsername());
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("menus", menuService.getRestaurantMenus(restaurant.getId()));
        return "manager/menu/list";
    }

    // 새 메뉴 추가 폼
    @GetMapping("/menus/new")
    public String newMenuForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        RestaurantDto restaurant = restaurantService.getRestaurantByManagerEmail(userDetails.getUsername());
        model.addAttribute("restaurantId", restaurant.getId());
        model.addAttribute("menu", new MenuDto());
        return "manager/menu/form";
    }

    // 메뉴 수정 폼
    @GetMapping("/menus/{id}/edit")
    public String editMenuForm(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        RestaurantDto restaurant = restaurantService.getRestaurantByManagerEmail(userDetails.getUsername());
        MenuDto menu = menuService.getMenuById(id);
        
        // 메뉴가 해당 레스토랑의 것인지 확인
        if (!menu.getRestaurantId().equals(restaurant.getId())) {
            throw new EntityNotFoundException("Menu not found");
        }
        
        model.addAttribute("restaurantId", restaurant.getId());
        model.addAttribute("menu", menu);
        return "manager/menu/form";
    }

    // 새 메뉴 추가 처리
    @PostMapping("/menus")
    public String createMenu(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute MenuDto menuDto,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {
        
        try {
            RestaurantDto restaurant = restaurantService.getRestaurantByManagerEmail(userDetails.getUsername());
            menuDto.setRestaurantId(restaurant.getId());

            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = Paths.get("uploads", uniqueFileName);
                Files.copy(imageFile.getInputStream(), filePath);
                menuDto.setImageUrl("/uploaded_images/" + uniqueFileName);
            }

            menuService.createMenu(menuDto);
            redirectAttributes.addFlashAttribute("successMessage", "메뉴가 성공적으로 추가되었습니다.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "메뉴 추가 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/manager/restaurant/menus";
    }

    // 메뉴 수정 처리
    @PostMapping("/menus/{id}")
    public String updateMenu(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute MenuDto menuDto,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {
        
        try {
            RestaurantDto restaurant = restaurantService.getRestaurantByManagerEmail(userDetails.getUsername());
            MenuDto currentMenu = menuService.getMenuById(id);
            
            // 메뉴가 해당 레스토랑의 것인지 확인
            if (!currentMenu.getRestaurantId().equals(restaurant.getId())) {
                throw new EntityNotFoundException("Menu not found");
            }

            menuDto.setId(id);
            menuDto.setRestaurantId(restaurant.getId());

            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = Paths.get("uploads", uniqueFileName);
                Files.copy(imageFile.getInputStream(), filePath);
                menuDto.setImageUrl("/uploaded_images/" + uniqueFileName);
            } else {
                menuDto.setImageUrl(currentMenu.getImageUrl());
            }

            menuService.updateMenu(id, menuDto);
            redirectAttributes.addFlashAttribute("successMessage", "메뉴가 성공적으로 수정되었습니다.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "메뉴 수정 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/manager/restaurant/menus";
    }

    // 메뉴 삭제 처리
    @DeleteMapping("/menus/{id}")
    public String deleteMenu(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        try {
            RestaurantDto restaurant = restaurantService.getRestaurantByManagerEmail(userDetails.getUsername());
            MenuDto menu = menuService.getMenuById(id);
            
            // 메뉴가 해당 레스토랑의 것인지 확인
            if (!menu.getRestaurantId().equals(restaurant.getId())) {
                throw new EntityNotFoundException("Menu not found");
            }

            menuService.deleteMenu(id);
            redirectAttributes.addFlashAttribute("successMessage", "메뉴가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "메뉴 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/manager/restaurant/menus";
    }
} 