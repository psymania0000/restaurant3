package com.restaurant.controller;

import com.restaurant.dto.RestaurantDTO;
import com.restaurant.dto.ReviewDTO;
import com.restaurant.dto.MenuDTO;
import com.restaurant.service.MenuService;
import com.restaurant.service.RestaurantService;
import com.restaurant.service.ReviewService;
import com.restaurant.service.UserService;
import com.restaurant.entity.User;
import com.restaurant.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final ReviewService reviewService;
    private final UserService userService;

    @GetMapping("/{id}")
    public String getRestaurantDetail(@PathVariable Long id, Model model) {
        RestaurantDTO restaurant = restaurantService.getRestaurantById(id);
        model.addAttribute("restaurant", restaurant);

        // 메뉴 목록 추가
        List<MenuDTO> menus = menuService.getRestaurantMenus(id);
        model.addAttribute("menus", menus);

        // 후기 목록 추가
        List<ReviewDTO> reviews = reviewService.getReviewsByRestaurantId(id);
        model.addAttribute("reviews", reviews);

        // 사용자의 현재 포인트 정보 추가
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            model.addAttribute("userPoints", userDetails.getUser().getPoints());
        } else {
            model.addAttribute("userPoints", 0); // 비로그인 시 0 포인트
        }

        return "restaurant/detail";
    }

    // 맛집 목록 페이지 (HTML)
    @GetMapping("")
    public String restaurantListPage(Model model) {
        model.addAttribute("restaurants", restaurantService.getAllRestaurants());
        return "restaurant/list";
    }

    @GetMapping("/new")
    public String newRestaurantForm(Model model) {
        model.addAttribute("restaurant", new RestaurantDTO());
        return "restaurant/form";
    }

    @GetMapping("/{id}/edit")
    public String editRestaurantForm(@PathVariable Long id, Model model) {
        model.addAttribute("restaurant", restaurantService.getRestaurantById(id));
        return "restaurant/form";
    }

    @PostMapping("/{id}/delete")
    public String deleteRestaurant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        restaurantService.deleteRestaurant(id);
        redirectAttributes.addFlashAttribute("message", "레스토랑이 삭제되었습니다.");
        return "redirect:/restaurants";
    }

    @PutMapping("/{id}/info")
    public ResponseEntity<RestaurantDTO> updateRestaurantInfo(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam String phone,
            @RequestParam String email) {
        RestaurantDTO updatedRestaurant = restaurantService.updateRestaurantInfo(id, name, address, phone, email);
        return ResponseEntity.ok(updatedRestaurant);
    }
}

// API 전용 컨트롤러 분리
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
class RestaurantApiController {
    private final RestaurantService restaurantService;

    @GetMapping("")
    public List<RestaurantDTO> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    @GetMapping("/{id}")
    public RestaurantDTO getRestaurantById(@PathVariable Long id) {
        return restaurantService.getRestaurantById(id);
    }
} 