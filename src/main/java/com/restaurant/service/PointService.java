package com.restaurant.service;

import com.restaurant.entity.User;
import com.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserRepository userRepository;

    @Transactional
    public void addPoints(Long userId, int points, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        user.setPoints(user.getPoints() + points);
        userRepository.save(user);
        
        // TODO: 포인트 적립 내역 저장 로직 구현
    }

    @Transactional
    public void usePoints(Long userId, int points, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        if (user.getPoints() < points) {
            throw new RuntimeException("포인트가 부족합니다.");
        }
        
        user.setPoints(user.getPoints() - points);
        userRepository.save(user);
        
        // TODO: 포인트 사용 내역 저장 로직 구현
    }

    @Transactional
    public void addReservationPoints(Long userId, int numberOfPeople) {
        // 예약 인원당 100포인트 적립
        int points = numberOfPeople * 100;
        addPoints(userId, points, "예약 포인트 적립");
    }

    @Transactional
    public void addReviewPoints(Long userId) {
        // 리뷰 작성시 50포인트 적립
        addPoints(userId, 50, "리뷰 작성 포인트 적립");
    }

    @Transactional
    public void usePointsForDiscount(Long userId, int amount) {
        // 100포인트 = 1,000원 할인
        int pointsNeeded = amount / 10;
        usePoints(userId, pointsNeeded, "포인트 할인 사용");
    }
} 