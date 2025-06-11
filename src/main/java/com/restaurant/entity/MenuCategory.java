package com.restaurant.entity;

public enum MenuCategory {
    APPETIZER("전채"),
    MAIN("메인"),
    SIDE("사이드"),
    DESSERT("디저트"),
    BEVERAGE("음료");

    private final String displayName;

    MenuCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 