package nus.iss.se.magicbag.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.auth.UserContextHolder;
import nus.iss.se.magicbag.auth.service.LoginFailureHandler;
import nus.iss.se.magicbag.auth.service.LoginSuccessHandler;
import nus.iss.se.magicbag.auth.service.TokenCacheService;
import nus.iss.se.magicbag.dto.Result;
import nus.iss.se.magicbag.exception.ResultEnum;
import nus.iss.se.magicbag.filter.JwtAuthenticationFilter;
import nus.iss.se.magicbag.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityChainConfig {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final UserContextHolder userContextHolder;
    private final UserDetailsService userDetailsService;
    private final TokenCacheService tokenCacheService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/*").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()  // 放行 OPTIONS 预检
                        .anyRequest().authenticated()
                )
//                .formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                                .usernameParameter("username")
                                .passwordParameter("password")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                )
//                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> handleAuthenticationFail(response,authException))
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService, tokenCacheService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void handleAuthenticationFail(HttpServletResponse response, AuthenticationException e) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
        String json = objectMapper.writeValueAsString(Result.error(ResultEnum.FAIL,"Please login first"));
        response.getWriter().write(json);
    }
    // CORS 配置
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*")); // 允许所有 localhost 端口
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // 如果需要 Cookie 认证
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
