package com.example.chalpuplatform.oauth.config;

import com.example.chalpuplatform.oauth.security.jwt.JwtAuthenticationFilter;
import com.example.chalpuplatform.oauth.security.oauth2.CustomOAuth2UserService;
import com.example.chalpuplatform.oauth.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.example.chalpuplatform.oauth.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthorizationRequestResolver customAuthorizationRequestResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 공개 경로
                        .requestMatchers("/", "/login", "/login.html", "/error", "/favicon.ico","/util/token","/actuator/**").permitAll()
                        .requestMatchers("/index.html","/test.html","/landing/**").permitAll()
                        .requestMatchers("/api/auth/refresh", "/api/auth/logout", "/api/auth/me").permitAll()
                        .requestMatchers("/api/test/**").permitAll() // 테스트용 엔드포인트 전체 허용
                        .requestMatchers("/admin/cleanup-tokens").permitAll() // 임시 관리자 엔드포인트
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Apple 테스트 경로 허용
                        .requestMatchers("/api/auth/apple/**","/api/auth/naver/**","/api/auth/kakao/**","/api/auth/google/**").permitAll()
                        .requestMatchers("/apple-login-test.html").permitAll()
                        // OAuth2 관련 경로
                        .requestMatchers("/api/oauth2/**", "/api/login/oauth2/**").permitAll()
                        // 캠페인 관련 경로
                        .requestMatchers("/api/campaigns/types").permitAll()
                        .requestMatchers("/api/campaigns/sns-platforms").permitAll()
                        .requestMatchers("/api/campaigns/campaign-platforms").permitAll()
                        // 공지사항/홈 광고 배너 관련 경로
                        .requestMatchers("/api/notices/**").permitAll()
                        // 앱 버전 관련 경로
                        .requestMatchers("/api/home/version/**").permitAll()
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .baseUri("/api/oauth2/authorization")
                                .authorizationRequestResolver(customAuthorizationRequestResolver)
                        )
                        .redirectionEndpoint(endpoint -> endpoint.baseUri("/api/login/oauth2/code/*"))
                        .userInfoEndpoint(endpoint -> endpoint.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 웹 + 모바일 모두 허용
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "*"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
