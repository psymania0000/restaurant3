package com.restaurant.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class SmsService {
    @Value("${sms.api-key}")
    private String apiKey;

    @Value("${sms.sender-number}")
    private String senderNumber;

    public String sendVerificationCode(String phoneNumber) {
        // 실제 SMS 서비스 연동 코드는 여기에 구현
        // 현재는 테스트를 위해 랜덤 코드 생성
        String verificationCode = generateVerificationCode();
        // TODO: 실제 SMS 발송 로직 구현
        return verificationCode;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
} 