package com.restaurant.controller;

import com.restaurant.service.MenuService;
import com.restaurant.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private RestaurantService restaurantService;
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("menus", menuService.getAllMenus());
        model.addAttribute("restaurants", restaurantService.getAllRestaurants());
        return "index";
    }
} 