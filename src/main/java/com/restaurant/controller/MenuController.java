package com.restaurant.controller;

import com.restaurant.dto.MenuDto;
import com.restaurant.service.MenuService;
import com.restaurant.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<MenuDto> createMenu(
            @RequestParam("restaurantId") Long restaurantId,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") String price,
            @RequestParam("category") String category,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        MenuDto menuDto = new MenuDto();
        menuDto.setRestaurantId(restaurantId);
        menuDto.setName(name);
        menuDto.setDescription(description);
        // String price를 Double로 변환하여 설정
        try {
            menuDto.setPrice(Double.parseDouble(price));
        } catch (NumberFormatException e) {
            // 가격 변환 오류 처리 (예: 기본값 설정 또는 예외 발생)
            // 여기서는 간단히 0.0으로 설정
            menuDto.setPrice(0.0);
        }
        menuDto.setCategory(category);

        if (image != null && !image.isEmpty()) {
            String fileName = fileStorageService.storeFile(image);
            menuDto.setImageUrl("/api/menu-images/" + fileName);
        }

        return ResponseEntity.ok(menuService.createMenu(menuDto));
    }

    @PutMapping("/{menuId}")
    public ResponseEntity<MenuDto> updateMenu(
            @PathVariable Long menuId,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") String price,
            @RequestParam("category") String category,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        MenuDto menuDto = new MenuDto();
        menuDto.setId(menuId);
        menuDto.setName(name);
        menuDto.setDescription(description);
        // String price를 Double로 변환하여 설정
         try {
             menuDto.setPrice(Double.parseDouble(price));
         } catch (NumberFormatException e) {
             // 가격 변환 오류 처리
             menuDto.setPrice(0.0);
         }
        menuDto.setCategory(category);

        if (image != null && !image.isEmpty()) {
            String fileName = fileStorageService.storeFile(image);
            menuDto.setImageUrl("/api/menu-images/" + fileName);
        } else {
            // 기존 이미지 URL을 유지하는 로직 필요
            // MenuDto에 imageUrl 필드가 있다면 기존 값을 가져와서 설정
            // 또는 MenuService에서 업데이트 로직에 따라 처리
        }

        return ResponseEntity.ok(menuService.updateMenu(menuId, menuDto));
    }

    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuDto>> getRestaurantMenus(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getRestaurantMenus(restaurantId));
    }
} 