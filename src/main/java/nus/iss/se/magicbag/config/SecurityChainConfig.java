package nus.iss.se.magicbag.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // 明确告诉 Spring Security 使用我们下面定义的 corsConfigurationSource Bean
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // 公开访问的路径（按优先级顺序）
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/user/register").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/api/product/**").permitAll()
                        .requestMatchers("/error").permitAll()  // Spring Boot 错误处理路径
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> handleAuthenticationFail(request, response, authException))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void handleAuthenticationFail(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
        String requestPath = request.getRequestURI();
        

        if (requestPath.startsWith("/api/auth/")) {
            log.warn("Authentication failed on public path: {} - This should not happen!", requestPath);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            String json = objectMapper.writeValueAsString(Result.error(ResultStatus.FAIL, "Request failed. Please check your credentials."));
            response.getWriter().write(json);
            return;
        }
        
        // 对于需要认证的路径，返回认证错误
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String authHeader = request.getHeader("Authorization");
        boolean hasToken = authHeader != null && authHeader.startsWith("Bearer ");
        
        log.error("Authentication failed - Path: {}, Has Token: {}, Error: {}", 
                requestPath, hasToken, e.getClass().getSimpleName());
        
        String errorMessage = "Please login first";
        if (!hasToken) {
            errorMessage = "Please login first: Token missing in Authorization header";
        } else if (authHeader != null) {
            errorMessage = "Please login first: Invalid or expired token";
        }
        
        String json = objectMapper.writeValueAsString(Result.error(ResultStatus.FAIL, errorMessage));
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
