package com.restaurant.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final Key key;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    public JwtTokenProvider(@Value("${app.jwtSecret}") String jwtSecret) {
        // JWT 비밀 키를 Base64 디코딩하여 Key 객체 생성
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // JWT 토큰 생성
    public String generateToken(Authentication authentication) {
        com.restaurant.entity.User userPrincipal = (com.restaurant.entity.User) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .claims() // 클레임 설정 시작
                    .subject(userPrincipal.getId().toString()) // 사용자 ID를 Subject로 사용
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .and() // 클레임 설정 끝, 빌더로 돌아감
                .signWith(key) // HS512 알고리즘과 Key 사용 (알고리즘은 Key에서 추론됨)
                .compact();
    }

    // JWT 토큰에서 사용자 이름(Subject) 가져오기
    public String getUsernameFromJWT(String token) {
        // 최신 JJWT 버전에서 JwtParser 생성 방식
        return Jwts.parser()
                .setSigningKey(key) // 서명 키 설정 (deprecated 될 수 있지만 parserBuilder 오류 해결 우선)
                // .verifyWith(key) // Recommended in newer versions, but might require different setup
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // JWT 토큰 유효성 검증
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .setSigningKey(key) // 서명 키 설정 (deprecated 될 수 있지만 parserBuilder 오류 해결 우선)
                    // .verifyWith(key) // Recommended in newer versions
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        } catch (io.jsonwebtoken.security.SignatureException ex) { // Corrected SignatureException import
             logger.error("Invalid JWT signature");
        }
        return false;
    }
} 