package com.restaurant.service;

import com.restaurant.dto.LoginRequest;
import com.restaurant.dto.SignupRequest;
import com.restaurant.dto.UserDTO;
import com.restaurant.dto.UserUpdateDTO;
import com.restaurant.entity.User;
import com.restaurant.model.UserRole;
import com.restaurant.repository.UserRepository;
import com.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.persistence.EntityNotFoundException;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestaurantRepository restaurantRepository;

    @PostConstruct
    public void init() {
        createAdminIfNotExists();
    }

    private void createAdminIfNotExists() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("*l82374840"));
            admin.setName("관리자");
            admin.setEmail("admin@restaurant.com");
            admin.setPhone("010-0000-0000");
            admin.setAddress("서울시");
            admin.setRole(UserRole.ADMIN);
            admin.setPoints(0);
            admin.setEnabled(true);
            userRepository.save(admin);
        }
    }

    public boolean isUsernameAvailable(String username) {
        log.info("아이디 사용 가능 여부 확인: {}", username);
        boolean exists = userRepository.existsByUsername(username);
        log.info("아이디 사용 가능 여부 결과: {}, exists={}", username, exists);
        return !exists;
    }

    public boolean isPhoneAvailable(String phone) {
        log.info("전화번호 사용 가능 여부 확인: {}", phone);
        boolean exists = userRepository.existsByPhone(phone);
        log.info("전화번호 사용 가능 여부 결과: {}, exists={}", phone, exists);
        return !exists;
    }

    @Transactional
    public User signup(SignupRequest request) {
        log.info("회원가입 서비스 시작: {}", request.getUsername());
        
        // 필수 필드 검증
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            log.error("아이디 누락");
            throw new RuntimeException("아이디를 입력해주세요.");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            log.error("비밀번호 누락");
            throw new RuntimeException("비밀번호를 입력해주세요.");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            log.error("이름 누락");
            throw new RuntimeException("이름을 입력해주세요.");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            log.error("이메일 누락");
            throw new RuntimeException("이메일을 입력해주세요.");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            log.error("전화번호 누락");
            throw new RuntimeException("전화번호를 입력해주세요.");
        }

        // 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("이메일 중복: {}", request.getEmail());
            throw new RuntimeException("이미 등록된 이메일입니다.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.error("아이디 중복: {}", request.getUsername());
            throw new RuntimeException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            log.error("전화번호 중복: {}", request.getPhone());
            throw new RuntimeException("이미 등록된 전화번호입니다.");
        }

        // 비밀번호 유효성 검사
        if (request.getPassword().length() < 8) {
            log.error("비밀번호 길이 부족");
            throw new RuntimeException("비밀번호는 8자 이상이어야 합니다.");
        }

        try {
            log.info("사용자 엔티티 생성 시작");
            User user = new User();
            user.setUsername(request.getUsername());
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setAddress(request.getAddress());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(UserRole.USER);
            user.setPoints(100);
            user.setEnabled(true);

            log.info("사용자 저장 시작");
            User savedUser = userRepository.save(user);
            log.info("사용자 저장 완료: {}", savedUser.getId());
            
            return savedUser;
        } catch (Exception e) {
            log.error("회원가입 처리 중 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setAddress(userDTO.getAddress());
        user.setRole(userDTO.getRole());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void updateUserRole(Long userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional
    public UserDTO changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Transactional(readOnly = true)
    public UserDTO convertToDTO(User user) {
        UserDTO.UserDTOBuilder builder = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .points(user.getPoints())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt());

        // 관리 식당 이름 리스트 세팅 (email 기준)
        java.util.List<String> managedRestaurantNames = restaurantRepository.findByEmail(user.getEmail())
            .map(r -> java.util.List.of(r.getName()))
            .orElseGet(java.util.ArrayList::new);
        builder.managedRestaurantNames(managedRestaurantNames);

        return builder.build();
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Transactional
    public void addPoints(Long userId, int points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setPoints(user.getPoints() + points);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsernameWithRestaurants(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO login(LoginRequest loginRequest) {
        User user = userRepository.findByUsernameWithRestaurants(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("등록되지 않은 아이디입니다."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return convertToDTO(user);
    }

    @Transactional
    public void withdrawUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setAddress(userDTO.getAddress());
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setPoints(0);
        return convertToDTO(userRepository.save(user));
    }

    @Transactional
    public UserDTO lockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(false);
        return convertToDTO(userRepository.save(user));
    }

    @Transactional
    public UserDTO unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(true);
        return convertToDTO(userRepository.save(user));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public long getUserCount() {
        return userRepository.count();
    }

    @Transactional
    public UserDTO updateUserProfile(String username, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 이메일 변경 (선택 사항)
        if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().isEmpty()) {
            user.setEmail(userUpdateDTO.getEmail());
        }

        // 이름 변경 (선택 사항)
        if (userUpdateDTO.getName() != null && !userUpdateDTO.getName().isEmpty()) {
            user.setName(userUpdateDTO.getName());
        }

        // 비밀번호 변경 (선택 사항)
        if (userUpdateDTO.getNewPassword() != null && !userUpdateDTO.getNewPassword().isEmpty()) {
            if (userUpdateDTO.getCurrentPassword() == null || !passwordEncoder.matches(userUpdateDTO.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }
            user.setPassword(passwordEncoder.encode(userUpdateDTO.getNewPassword()));
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public User createUserEntity(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setAddress(userDTO.getAddress());
        user.setRole(userDTO.getRole());
        user.setEnabled(true);
        user.setPoints(0);
        return user;
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsernameWithRestaurants(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
} 