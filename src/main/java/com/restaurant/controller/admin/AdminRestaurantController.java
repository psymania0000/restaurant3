package com.restaurant.controller.admin;

import com.restaurant.dto.RestaurantDto;
import com.restaurant.service.RestaurantService;
import com.restaurant.service.MenuService;
import com.restaurant.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/restaurants")
@RequiredArgsConstructor
public class AdminRestaurantController {

    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final ReviewService reviewService;

    @Value("${app.upload.dir}") // 이미지 업로드 디렉토리 경로 주입
    private String uploadDir;

    // 식당 목록 조회
    @GetMapping
    public String listRestaurants(Model model) {
        // TODO: Fetch list of all restaurants
        List<RestaurantDto> restaurants = restaurantService.getAllRestaurants(); // Assuming getAllRestaurants() exists or will be created
        model.addAttribute("restaurants", restaurants);
        return "admin/restaurant/list";
    }

    // 식당 상세 정보 조회
    @GetMapping("/{id}")
    public String viewRestaurantDetail(@PathVariable Long id, Model model) {
        RestaurantDto restaurant = restaurantService.getRestaurantById(id);
        model.addAttribute("restaurant", restaurant);
        
        // 메뉴 목록 추가
        List<com.restaurant.dto.MenuDto> menus = menuService.getRestaurantMenus(id);
        model.addAttribute("menus", menus);
        
        // 후기 목록 추가
        List<com.restaurant.dto.ReviewDto> reviews = reviewService.getReviewsByRestaurantId(id);
        model.addAttribute("reviews", reviews);
        
        return "admin/restaurant/detail";
    }

    // 식당 추가 폼
    @GetMapping("/new")
    public String newRestaurantForm(Model model) {
        model.addAttribute("restaurant", new RestaurantDto());
        return "admin/restaurant/form";
    }

    // 새 메뉴 추가 폼
    @GetMapping("/{restaurantId}/menus/new")
    public String newMenuForm(@PathVariable Long restaurantId, Model model) {
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("menu", new com.restaurant.dto.MenuDto());
        return "admin/menu/form";
    }

    // 메뉴 수정 폼 보여주기
    @GetMapping("/{restaurantId}/menus/{menuId}/edit")
    public String editMenuForm(@PathVariable Long restaurantId, @PathVariable Long menuId, Model model) {
        com.restaurant.dto.MenuDto menu = menuService.getMenuById(menuId);
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("menu", menu);
        return "admin/menu/form";
    }

    // 새 메뉴 추가 처리
    @PostMapping("/{restaurantId}/menus")
    public String createMenu(@PathVariable Long restaurantId, @ModelAttribute com.restaurant.dto.MenuDto menuDto, RedirectAttributes redirectAttributes) {
        menuDto.setRestaurantId(restaurantId); // Ensure restaurantId is set in DTO
        try {
            menuService.createMenu(menuDto);
            redirectAttributes.addFlashAttribute("successMessage", "메뉴가 성공적으로 추가되었습니다.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "식당을 찾을 수 없습니다: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "메뉴 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/restaurants/" + restaurantId;
    }

    // 식당 추가 처리
    @PostMapping
    public String createRestaurant(@ModelAttribute RestaurantDto restaurantDto, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile, RedirectAttributes redirectAttributes) {
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // String uploadDir = "src/main/resources/static/images/restaurants"; // 하드코딩된 경로 제거
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = Paths.get(uploadDir, uniqueFileName);
                Files.copy(imageFile.getInputStream(), filePath);
                restaurantDto.setImageUrl("/uploaded_images/" + uniqueFileName); // 이미지 URL 프리픽스 변경
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("message", "Error uploading image: " + e.getMessage());
                return "redirect:/admin/restaurants/new";
            }
        }
        restaurantService.createRestaurant(restaurantDto);
        redirectAttributes.addFlashAttribute("message", "식당이 성공적으로 추가되었습니다.");
        return "redirect:/admin/restaurants";
    }

    // 식당 수정 폼
    @GetMapping("/{id}/edit")
    public String editRestaurantForm(@PathVariable Long id, Model model) {
        model.addAttribute("restaurant", restaurantService.getRestaurantById(id));
        return "admin/restaurant/form";
    }

    // 식당 수정 처리
    @PostMapping("/{id}")
    public String updateRestaurant(@PathVariable Long id, @ModelAttribute RestaurantDto restaurantDto, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile, RedirectAttributes redirectAttributes) {
         if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // String uploadDir = "src/main/resources/static/images/restaurants"; // 하드코딩된 경로 제거
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = Paths.get(uploadDir, uniqueFileName);
                Files.copy(imageFile.getInputStream(), filePath);
                restaurantDto.setImageUrl("/uploaded_images/" + uniqueFileName); // 이미지 URL 프리픽스 변경
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("message", "Error uploading image: " + e.getMessage());
                return "redirect:/admin/restaurants/" + id + "/edit";
            }
        } else if (restaurantService.getRestaurantById(id).getImageUrl() != null) {
             // If no new image is uploaded, keep the existing image URL
             // restaurantDto.setImageUrl(restaurantService.getRestaurantById(id).getImageUrl()); // 이 로직은 Service 단에서 처리하는 것이 더 적절할 수 있습니다.
        }
        // 이미지 URL 처리를 Service 단으로 옮기는 것을 고려하여 주석 처리하거나 수정할 수 있습니다.
        // 현재는 DTO에 이미지가 없으면 기존 이미지 URL이 넘어오지 않으므로 여기서 가져와 설정합니다.
         if (restaurantDto.getImageUrl() == null || restaurantDto.getImageUrl().isEmpty()) {
             RestaurantDto existingRestaurant = restaurantService.getRestaurantById(id);
             restaurantDto.setImageUrl(existingRestaurant.getImageUrl());
         }

        restaurantService.updateRestaurant(id, restaurantDto);
        redirectAttributes.addFlashAttribute("message", "식당이 성공적으로 수정되었습니다.");
        return "redirect:/admin/restaurants";
    }

    // 식당 삭제 처리
    @PostMapping("/{id}/delete")
    public String deleteRestaurant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        restaurantService.deleteRestaurant(id);
        redirectAttributes.addFlashAttribute("message", "식당이 성공적으로 삭제되었습니다.");
        return "redirect:/admin/restaurants";
    }

    // 메뉴 수정 처리
    @PostMapping("/{restaurantId}/menus/{menuId}")
    public String updateMenu(@PathVariable Long restaurantId, @PathVariable Long menuId, @ModelAttribute com.restaurant.dto.MenuDto menuDto, RedirectAttributes redirectAttributes) {
        menuDto.setId(menuId); // Ensure menuId is set in DTO
        menuDto.setRestaurantId(restaurantId); // Ensure restaurantId is set in DTO
        try {
            menuService.updateMenu(menuId, menuDto);
            redirectAttributes.addFlashAttribute("successMessage", "메뉴가 성공적으로 수정되었습니다.");
        } catch (EntityNotFoundException e) {
             redirectAttributes.addFlashAttribute("errorMessage", "메뉴를 찾을 수 없습니다: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "메뉴 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/restaurants/" + restaurantId;
    }

    // 메뉴 삭제 처리
    @DeleteMapping("/{restaurantId}/menus/{menuId}")
    public String deleteMenu(@PathVariable Long restaurantId, @PathVariable Long menuId, RedirectAttributes redirectAttributes) {
        try {
            menuService.deleteMenu(menuId);
            redirectAttributes.addFlashAttribute("successMessage", "메뉴가 성공적으로 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "메뉴를 찾을 수 없습니다: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "메뉴 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/restaurants/" + restaurantId;
    }

    // 후기 삭제 처리 (수정)
    @DeleteMapping("/reviews/{id}")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long restaurantId = null;
        try {
            // 후기 정보를 먼저 가져와서 식당 ID를 얻습니다.
            com.restaurant.dto.ReviewDto review = reviewService.getReviewById(id); // ReviewService에 getReviewById 메서드가 필요
            restaurantId = review.getRestaurantId();
            
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "후기가 성공적으로 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "후기를 찾을 수 없습니다: " + e.getMessage());
            // 후기를 찾을 수 없는 경우, 식당 목록 페이지로 리다이렉트하거나 다른 처리를 할 수 있습니다.
            return "redirect:/admin/restaurants";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "후기 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        // 삭제 후 해당 식당 상세 페이지로 리다이렉트
        return "redirect:/admin/restaurants/" + (restaurantId != null ? restaurantId : ""); // restaurantId가 없을 경우 예외 처리 필요
    }
} 