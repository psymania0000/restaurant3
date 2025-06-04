package com.restaurant.service;

import com.restaurant.entity.User;
import com.restaurant.repository.UserRepository;
import com.restaurant.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        
        User user = processOAuth2User(provider, oauth2User);
        return new CustomOAuth2User(user);
    }

    private User processOAuth2User(String provider, OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = getEmail(provider, attributes);
        String name = getName(provider, attributes);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setName(name);
            return userRepository.save(user);
        }

        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setRole("ROLE_USER");
        user.setPoints(100);
        user.setCreatedAt(java.time.LocalDateTime.now());
        return userRepository.save(user);
    }

    @SuppressWarnings("unchecked")
    private String getEmail(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("email");
        } else if ("kakao".equals(provider)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                return (String) kakaoAccount.get("email");
            }
        } else if ("naver".equals(provider)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            if (response != null) {
                return (String) response.get("email");
            }
        }
        throw new OAuth2AuthenticationException("Unsupported provider or missing email: " + provider);
    }

    @SuppressWarnings("unchecked")
    private String getName(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("name");
        } else if ("kakao".equals(provider)) {
            Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
            if (properties != null) {
                return (String) properties.get("nickname");
            }
        } else if ("naver".equals(provider)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            if (response != null) {
                return (String) response.get("name");
            }
        }
        throw new OAuth2AuthenticationException("Unsupported provider or missing name: " + provider);
    }

    @SuppressWarnings("unchecked")
    private String getProviderId(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("sub");
        } else if ("kakao".equals(provider)) {
            return String.valueOf(attributes.get("id"));
        } else if ("naver".equals(provider)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            if (response != null) {
                return (String) response.get("id");
            }
        }
        throw new OAuth2AuthenticationException("Unsupported provider or missing id: " + provider);
    }
} 