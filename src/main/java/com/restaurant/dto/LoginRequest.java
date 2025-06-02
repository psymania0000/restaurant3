package com.restaurant.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "전화번호를 입력해주세요")
    private String phoneNumber;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
} 