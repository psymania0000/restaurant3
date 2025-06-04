package com.restaurant.service;

import com.restaurant.dto.LoginRequest;
import com.restaurant.dto.SignupRequest;
import com.restaurant.dto.UserDto;
import com.restaurant.entity.User;
import com.restaurant.model.Role;
import com.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signup(SignupRequest request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("이미 등록된 전화번호입니다.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER"); // Role enum 대신 문자열 사용
        user.setPoints(100); // 가입 시 기본 포인트 지급
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    // 모든 사용자 목록을 가져오는 메소드
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 특정 사용자 ID로 사용자 정보를 가져오는 메소드
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return convertToDto(user);
    }

    // 사용자 정보 업데이트 메소드
    @Transactional
    public UserDto updateUser(UserDto userDto) {
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userDto.getId()));

        user.setName(userDto.getName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setRole(userDto.getRole());
        user.setPoints(userDto.getPoints());
        // 비밀번호는 별도의 기능을 통해 수정하거나 여기서는 제외할 수 있습니다.

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    // 특정 사용자 ID로 사용자를 삭제하는 메소드
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with ID " + userId + " not found.");
        }
        userRepository.deleteById(userId);
    }

    // 사용자 역할 업데이트 메서드 추가
    @Transactional
    public void updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found."));
        user.setRole(role);
        userRepository.save(user);
    }

    // User 엔티티를 UserDto로 변환하는 메소드
    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .points(user.getPoints())
                .build();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return convertToDto(user);
    }

    // TODO: Add methods for login, user info management, etc.

    // 임시 메서드 - verifyPhone 기능 구현 필요
    public boolean verifyPhone(String phoneNumber) {
        // 실제 전화번호 인증 로직 구현
        return true; // 임시로 true 반환
    }

    // 임시 메서드 - addPoints 기능 구현 필요
    @Transactional
    public void addPoints(Long userId, int points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setPoints(user.getPoints() + points);
        userRepository.save(user);
    }

    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public UserDto login(LoginRequest loginRequest) {
        User user = userRepository.findByPhoneNumber(loginRequest.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("등록되지 않은 전화번호입니다."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return convertToDto(user);
    }

} 