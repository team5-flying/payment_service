package com.bootcamp.paymentdemo.common.config;

import com.bootcamp.paymentdemo.common.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

import static org.springframework.boot.security.autoconfigure.web.servlet.PathRequest.toStaticResources;

/**
 * Spring Security 설정 - JWT 기반 인증
 *
 * TODO: 개선 사항
 * - CORS 설정 추가
 * - 역할 기반 접근 제어 (ROLE_ADMIN, ROLE_USER)
 * - API 엔드포인트별 세밀한 권한 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//                // TODO : 테스트 오픈 - CORS 설정 추가, 삭제 필요
//                .cors(cors -> cors.configurationSource(request -> {
//                    var config = new org.springframework.web.cors.CorsConfiguration();
//                    config.setAllowedOriginPatterns(List.of("*")); // 모든 도메인 허용 (테스트용)
//                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//                    config.setAllowedHeaders(List.of("*"));
//                    config.setAllowCredentials(true);
//                    return config;
//                }))

            // CSRF 비활성화 (JWT 사용 시 불필요)
            .csrf(AbstractHttpConfigurer::disable)

            // Session 사용 안 함 (Stateless)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Form Login 사용 안함
            .formLogin(AbstractHttpConfigurer::disable)

            // 요청 권한 설정
            .authorizeHttpRequests(authorize -> authorize
                     // 1) 정적 리소스
                    .requestMatchers(toStaticResources().atCommonLocations()).permitAll()

                    // 2) 템플릿 페이지 렌더링
                    .requestMatchers(HttpMethod.GET, "/").permitAll()
                    .requestMatchers(HttpMethod.GET, "/pages/**").permitAll()

                    // 3) 공개 API
                    .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()

                    // 4) 인증 API
                    .requestMatchers(HttpMethod.POST, "/api/login", "/api/signup", "/api/refresh").permitAll()

                    // 웹훅
                    .requestMatchers("/api/webhooks/**").permitAll()

                    // 5) 그 외 API는 인증 필요
                    .requestMatchers("/api/**").authenticated()

                    // 6) 나머지 전부 인증 필요
                    .anyRequest().authenticated()
            )

            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * PasswordEncoder Bean
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}
