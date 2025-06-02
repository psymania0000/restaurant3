package com.restaurant.service;

import com.restaurant.dto.MenuDto;
import com.restaurant.entity.Menu;
import com.restaurant.entity.Restaurant;
import com.restaurant.repository.MenuRepository;
import com.restaurant.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public MenuDto createMenu(MenuDto menuDto) {
        Restaurant restaurant = restaurantRepository.findById(menuDto.getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        Menu menu = new Menu();
        menu.setRestaurant(restaurant);
        menu.setName(menuDto.getName());
        menu.setDescription(menuDto.getDescription());
        menu.setPrice(new BigDecimal(menuDto.getPrice()));
        menu.setCategory(menuDto.getCategory());
        menu.setImageUrl(menuDto.getImageUrl());

        Menu savedMenu = menuRepository.save(menu);
        return convertToDto(savedMenu);
    }

    @Transactional
    public MenuDto updateMenu(Long menuId, MenuDto menuDto) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new EntityNotFoundException("Menu not found"));

        menu.setName(menuDto.getName());
        menu.setDescription(menuDto.getDescription());
        menu.setPrice(new BigDecimal(menuDto.getPrice()));
        menu.setCategory(menuDto.getCategory());
        if (menuDto.getImageUrl() != null) {
            menu.setImageUrl(menuDto.getImageUrl());
        } else if (menu.getImageUrl() != null) {
            // Keep the existing image URL if no new image is uploaded and there was an old one
            menuDto.setImageUrl(menu.getImageUrl());
        }

        Menu updatedMenu = menuRepository.save(menu);
        return convertToDto(updatedMenu);
    }

    @Transactional
    public void deleteMenu(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new EntityNotFoundException("Menu not found"));
        menuRepository.delete(menu);
    }

    public List<MenuDto> getRestaurantMenus(Long restaurantId) {
        return menuRepository.findByRestaurantId(restaurantId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public MenuDto getMenuById(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new EntityNotFoundException("Menu not found"));
        return convertToDto(menu);
    }

    private MenuDto convertToDto(Menu menu) {
        MenuDto dto = new MenuDto();
        dto.setId(menu.getId());
        // TODO: Handle case where restaurant might be null if not fetched eagerly
        if (menu.getRestaurant() != null) {
             dto.setRestaurantId(menu.getRestaurant().getId());
        }
        dto.setName(menu.getName());
        dto.setDescription(menu.getDescription());
        if (menu.getPrice() != null) {
            dto.setPrice(menu.getPrice().doubleValue());
        } else {
            dto.setPrice(0.0);
        }
        dto.setCategory(menu.getCategory());
        dto.setImageUrl(menu.getImageUrl());
        return dto;
    }

    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }
} 