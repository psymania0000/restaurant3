package com.restaurant.service;

import com.restaurant.dto.MenuDTO;
import com.restaurant.entity.Menu;
import com.restaurant.entity.MenuCategory;
import com.restaurant.entity.Restaurant;
import com.restaurant.repository.MenuRepository;
import com.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;
    private final String uploadDir = "src/main/resources/static/images/menus";

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Transactional
    public MenuDTO createMenu(MenuDTO menuDTO, MultipartFile image) {
        Menu menu = new Menu();
        menu.setName(menuDTO.getName());
        menu.setDescription(menuDTO.getDescription());
        menu.setPrice(menuDTO.getPrice());
        menu.setCategory(menuDTO.getCategory());
        menu.setAvailable(menuDTO.getAvailable() != null ? menuDTO.getAvailable() : true);
        
        Restaurant restaurant = restaurantRepository.findById(menuDTO.getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
        menu.setRestaurant(restaurant);

        if (image != null && !image.isEmpty()) {
            try {
                String originalFilename = image.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = Paths.get(uploadDir, uniqueFileName);
                Files.copy(image.getInputStream(), filePath);
                menu.setImageUrl("/images/menus/" + uniqueFileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload menu image", e);
            }
        }

        return convertToDTO(menuRepository.save(menu));
    }

    @Transactional(readOnly = true)
    public List<MenuDTO> getAllMenus() {
        return menuRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuDTO getMenuById(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found"));
        return convertToDTO(menu);
    }

    @Transactional(readOnly = true)
    public List<MenuDTO> getMenusByRestaurantId(Long restaurantId) {
        return menuRepository.findByRestaurantId(restaurantId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuDTO updateMenu(Long id, MenuDTO menuDTO, MultipartFile image) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found"));

        menu.setName(menuDTO.getName());
        menu.setDescription(menuDTO.getDescription());
        menu.setPrice(menuDTO.getPrice());
        menu.setCategory(menuDTO.getCategory());
        menu.setAvailable(menuDTO.getAvailable());

        if (image != null && !image.isEmpty()) {
            try {
                String originalFilename = image.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = Paths.get(uploadDir, uniqueFileName);
                Files.copy(image.getInputStream(), filePath);
                menu.setImageUrl("/images/menus/" + uniqueFileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload menu image", e);
            }
        }

        return convertToDTO(menuRepository.save(menu));
    }

    @Transactional
    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
    }

    private MenuDTO convertToDTO(Menu menu) {
        return MenuDTO.builder()
                .id(menu.getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .category(menu.getCategory())
                .available(menu.getAvailable())
                .restaurantId(menu.getRestaurant().getId())
                .imageUrl(menu.getImageUrl())
                .build();
    }

    @Transactional(readOnly = true)
    public List<MenuDTO> getRestaurantMenus(Long restaurantId) {
        return menuRepository.findByRestaurantId(restaurantId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleMenuAvailability(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Menu not found"));
        menu.setAvailable(!menu.getAvailable());
        menuRepository.save(menu);
    }

    @Transactional(readOnly = true)
    public long getMenuCount() {
        return menuRepository.count();
    }
} 