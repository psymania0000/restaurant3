package com.restaurant.controller.admin;

import com.restaurant.dto.ReviewDTO;
import com.restaurant.dto.RestaurantDTO;
import com.restaurant.dto.MenuDTO;
import com.restaurant.entity.MenuCategory;
import com.restaurant.service.RestaurantService;
import com.restaurant.service.ReviewService;
import com.restaurant.service.MenuService;
import com.restaurant.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.annotation.PostConstruct;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.Arrays;

@Controller
@RequestMapping("/admin/restaurants")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminRestaurantController {

    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final ReviewService reviewService;

    @Value("${app.upload.dir}") // 이미지 업로드 디렉토리 경로 주입
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    // 식당 목록 조회
    @GetMapping
    public String listRestaurants(Model model) {
        model.addAttribute("restaurants", restaurantService.getAllRestaurants());
        return "admin/restaurant/list";
    }

    // 식당 상세 정보 조회
    @GetMapping("/{id}")
    public String getRestaurant(@PathVariable Long id, Model model) {
        RestaurantDTO restaurant = restaurantService.getRestaurantById(id);
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("reviews", reviewService.getRestaurantReviews(id));
        model.addAttribute("menus", menuService.getRestaurantMenus(id));
        return "admin/restaurant/detail";
    }

    // 식당 추가 폼
    @GetMapping("/new")
    public String newRestaurantForm(Model model, org.springframework.security.web.csrf.CsrfToken csrfToken) {
        model.addAttribute("restaurantDTO", new RestaurantDTO());
        model.addAttribute("_csrf", csrfToken);
        return "admin/restaurant/form";
    }

    // 새 메뉴 추가 폼
    @GetMapping("/{id}/menus/new")
    public String newMenuForm(@PathVariable Long id, Model model) {
        model.addAttribute("restaurant", restaurantService.getRestaurantById(id));
        model.addAttribute("menu", new MenuDTO());
        model.addAttribute("categories", Arrays.asList(MenuCategory.values()));
        return "admin/restaurant/menu-form";
    }

    // 메뉴 수정 폼 보여주기
    @GetMapping("/{id}/menus/{menuId}/edit")
    public String editMenuForm(@PathVariable Long id, @PathVariable Long menuId, Model model) {
        model.addAttribute("restaurant", restaurantService.getRestaurantById(id));
        model.addAttribute("menu", menuService.getMenuById(menuId));
        model.addAttribute("categories", Arrays.asList(MenuCategory.values()));
        return "admin/restaurant/menu-form";
    }

    // 새 메뉴 추가 처리
    @PostMapping("/{id}/menus")
    public String createMenu(@PathVariable Long id,
                           @ModelAttribute MenuDTO menuDTO,
                           @RequestParam(value = "image", required = false) MultipartFile image,
                           RedirectAttributes redirectAttributes) {
        try {
            menuDTO.setRestaurantId(id);
            menuService.createMenu(menuDTO, image);
            redirectAttributes.addFlashAttribute("successMessage", "메뉴가 성공적으로 추가되었습니다.");
            return "redirect:/admin/restaurants/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "메뉴 추가 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/admin/restaurants/" + id + "/menus/new";
        }
    }

    // 식당 추가 처리
    @PostMapping
    public String createRestaurant(
            @ModelAttribute RestaurantDTO restaurantDTO,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            if (restaurantDTO.getName() == null || restaurantDTO.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("식당 이름은 필수 입력 항목입니다.");
            }
            if (restaurantDTO.getAddress() == null || restaurantDTO.getAddress().trim().isEmpty()) {
                throw new IllegalArgumentException("주소는 필수 입력 항목입니다.");
            }
            if (restaurantDTO.getEmail() == null || restaurantDTO.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("이메일은 필수 입력 항목입니다.");
            }
            if (restaurantDTO.getCategory() == null || restaurantDTO.getCategory().trim().isEmpty()) {
                throw new IllegalArgumentException("카테고리는 필수 입력 항목입니다.");
            }

            // 슈퍼 어드민은 모든 식당을 관리할 수 있음
            RestaurantDTO createdRestaurant = restaurantService.createRestaurant(restaurantDTO, image, userDetails.getUser().getEmail());
            redirectAttributes.addFlashAttribute("successMessage", "식당이 성공적으로 등록되었습니다.");
            return "redirect:/admin/restaurants";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "식당 등록 중 오류가 발생했습니다: " + e.getMessage());
            redirectAttributes.addFlashAttribute("restaurant", restaurantDTO);
            return "redirect:/admin/restaurants/new";
        }
    }

    // 식당 수정 폼
    @GetMapping("/{id}/edit")
    public String editRestaurantForm(@PathVariable Long id, Model model) {
        RestaurantDTO restaurant = restaurantService.getRestaurantById(id);
        model.addAttribute("restaurant", restaurant);
        return "admin/restaurant/form";
    }

    // 식당 수정 처리
    @PostMapping("/{id}")
    public String updateRestaurant(
            @PathVariable Long id,
            @ModelAttribute RestaurantDTO restaurantDTO,
            @RequestParam(value = "image", required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        try {
            RestaurantDTO updatedRestaurant = restaurantService.updateRestaurant(id, restaurantDTO, image);
            redirectAttributes.addFlashAttribute("successMessage", "식당 정보가 성공적으로 수정되었습니다.");
            return "redirect:/admin/restaurants";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "식당 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/admin/restaurants/" + id + "/edit";
        }
    }

    // 식당 삭제 처리
    @PostMapping("/{id}/delete")
    public String deleteRestaurant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            restaurantService.deleteRestaurant(id);
            redirectAttributes.addFlashAttribute("successMessage", "식당이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "식당 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/restaurants";
    }

    // 메뉴 수정 처리
    @PostMapping("/{id}/menus/{menuId}")
    public String updateMenu(@PathVariable Long id,
                           @PathVariable Long menuId,
                           @ModelAttribute MenuDTO menuDTO,
                           @RequestParam(value = "image", required = false) MultipartFile image,
                           RedirectAttributes redirectAttributes) {
        try {
            menuDTO.setRestaurantId(id);
            menuService.updateMenu(menuId, menuDTO, image);
            redirectAttributes.addFlashAttribute("successMessage", "메뉴가 성공적으로 수정되었습니다.");
            return "redirect:/admin/restaurants/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "메뉴 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/admin/restaurants/" + id + "/menus/" + menuId + "/edit";
        }
    }

    // 메뉴 삭제 처리
    @PostMapping("/{id}/menus/{menuId}/delete")
    public String deleteMenu(@PathVariable Long id,
                           @PathVariable Long menuId,
                           RedirectAttributes redirectAttributes) {
        try {
            menuService.deleteMenu(menuId);
            redirectAttributes.addFlashAttribute("successMessage", "메뉴가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "메뉴 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/restaurants/" + id;
    }

    // 후기 삭제 처리 (수정)
    @PostMapping("/{restaurantId}/reviews/{reviewId}/delete")
    public String deleteReview(
            @PathVariable Long restaurantId, 
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        reviewService.deleteReview(reviewId, userDetails.getUser().getEmail(), userDetails.getAuthorities());
        redirectAttributes.addFlashAttribute("message", "리뷰가 삭제되었습니다.");
        return "redirect:/admin/restaurants/" + restaurantId;
    }

    // 후기 목록 조회
    @GetMapping("/{id}/reviews")
    public String getRestaurantReviews(@PathVariable Long id, Model model) {
        model.addAttribute("restaurant", restaurantService.getRestaurantById(id));
        model.addAttribute("reviews", reviewService.getRestaurantReviews(id));
        return "admin/restaurant/reviews";
    }

    // 메뉴 목록 조회
    @GetMapping("/{id}/menus")
    public String getRestaurantMenus(@PathVariable Long id, Model model) {
        model.addAttribute("restaurant", restaurantService.getRestaurantById(id));
        model.addAttribute("menus", menuService.getRestaurantMenus(id));
        return "admin/restaurant/menus";
    }
} 