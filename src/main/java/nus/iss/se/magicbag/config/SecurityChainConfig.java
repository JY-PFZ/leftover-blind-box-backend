package nus.iss.se.magicbag.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.common.UserContextHolder;
import nus.iss.se.magicbag.auth.service.TokenCacheService;
import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import nus.iss.se.magicbag.auth.filter.JwtAuthenticationFilter;
import nus.iss.se.magicbag.util.JwtUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityChainConfig {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final UserContextHolder userContextHolder;
    private final UserDetailsService userDetailsService;
    private final TokenCacheService tokenCacheService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // 明确告诉 Spring Security 使用我们下面定义的 corsConfigurationSource Bean
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll() // 修正：改为 /api/auth/**
                        .requestMatchers("/api/user/register").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/api/product/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .requestMatchers("/api/payment/success").permitAll()
                        .requestMatchers("/api/payment/cancel").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> handleAuthenticationFail(response,authException))
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService, tokenCacheService, userContextHolder),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void handleAuthenticationFail(HttpServletResponse response, AuthenticationException e) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        log.error("Please login first: {}", ExceptionUtils.getStackTrace(e));
        String json = objectMapper.writeValueAsString(Result.error(ResultStatus.FAIL,"Please login first"));
        response.getWriter().write(json);
    }

    // CORS 配置
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许的前端来源 (使用 setAllowedOrigins 更为推荐)
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        // 允许的方法
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许的请求头
        config.setAllowedHeaders(List.of("*"));
        // 允许携带凭证
        config.setAllowCredentials(true);

        // ===============================================================
        // 关键的修复：将 x-new-token 暴露给前端
        // ===============================================================
        config.setExposedHeaders(List.of("x-new-token"));

        // 预检请求的缓存时间
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用此CORS配置
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
