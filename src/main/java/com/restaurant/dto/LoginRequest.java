package com.restaurant.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;  // Spring Security는 기본적으로 username을 사용
    private String password;
} 