package com.bootcamp.paymentdemo.common.security;

import com.bootcamp.paymentdemo.domain.member.entity.MemberRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private static final long ACCESS_EXP = 6 * 60 * 60 * 1000L; //3시간
    private static final long REFRESH_EXP = 14 * 24 * 60 * 60 * 1000L; //3시간


    private final SecretKey secretKey;

    public JwtProvider(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }


    public String createAccessToken(String email, MemberRole role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_EXP);

        return Jwts.builder()
                .subject(email)
                .claim("role", role.name())
                .claim("type", "ACCESS")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + REFRESH_EXP))
                .signWith(secretKey)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isRefreshToken(String token) {
        return "REFRESH".equals(getClaims(token).get("type", String.class));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT 만료됨: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 형식: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT 서명 검증 실패: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있거나 잘못됨: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT 검증 중 알 수 없는 오류", e);
        }
        return false;
    }
}
