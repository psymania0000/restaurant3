package com.restaurant.controller.api;

import com.restaurant.dto.MenuDTO;
import com.restaurant.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuApiController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuDTO>> getAllMenus() {
        List<MenuDTO> menus = menuService.getAllMenus();
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuDTO> getMenuById(@PathVariable Long id) {
        MenuDTO menu = menuService.getMenuById(id);
        return ResponseEntity.ok(menu);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuDTO>> getMenusByRestaurantId(@PathVariable Long restaurantId) {
        List<MenuDTO> menus = menuService.getMenusByRestaurantId(restaurantId);
        return ResponseEntity.ok(menus);
    }

    @PostMapping
    public ResponseEntity<MenuDTO> createMenu(
            @RequestPart("menu") MenuDTO menuDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        MenuDTO createdMenu = menuService.createMenu(menuDTO, image);
        return ResponseEntity.ok(createdMenu);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuDTO> updateMenu(
            @PathVariable Long id,
            @RequestPart("menu") MenuDTO menuDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        MenuDTO updatedMenu = menuService.updateMenu(id, menuDTO, image);
        return ResponseEntity.ok(updatedMenu);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<MenuDTO> toggleMenuAvailability(@PathVariable Long id) {
        menuService.toggleMenuAvailability(id);
        return ResponseEntity.ok(menuService.getMenuById(id));
    }
} 