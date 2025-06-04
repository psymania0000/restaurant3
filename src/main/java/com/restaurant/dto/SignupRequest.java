package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SignupRequest {
    
    @NotBlank(message = "아이디는 필수 입력값입니다")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력값입니다")
    private String password;

    @NotBlank(message = "이름은 필수 입력값입니다")
    private String name;

    @NotBlank(message = "전화번호는 필수 입력값입니다")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
    private String phoneNumber;
} 