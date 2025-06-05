package com.restaurant.security;

import com.restaurant.entity.User;
import com.restaurant.model.UserRole;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Getter
public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
        log.info("CustomUserDetails 생성: username={}, enabled={}, role={}", 
            user.getUsername(), user.getEnabled(), user.getRole());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = user.getRole().getRole();
        log.info("권한 조회: {}", role);
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        boolean enabled = user.getEnabled();
        log.info("계정 잠금 상태 확인: {}", enabled);
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        boolean enabled = user.getEnabled();
        log.info("계정 활성화 상태 확인: {}", enabled);
        return enabled;
    }

    public User getUser() {
        return user;
    }
} 