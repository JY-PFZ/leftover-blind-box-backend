package nus.iss.se.magicbag.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.auth.common.UserContextHolder;
import nus.iss.se.magicbag.auth.entity.MyUserDetails;
import nus.iss.se.magicbag.auth.service.TokenCacheService;
import nus.iss.se.magicbag.util.BaseUtil;
import nus.iss.se.magicbag.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenCacheService tokenCacheService;
    private final UserContextHolder userContextHolder;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        String token = BaseUtil.getTokenFromRequest(request);
        log.debug("JWT Filter - Request path: {}, Token present: {}", request.getRequestURI(), token != null);
        
        if (token != null && isValidToken(token)) {
            try {
                String username = jwtUtil.getClaims(token).getSubject();
                log.debug("JWT Filter - Token valid, username: {}", username);

                // 加载用户信息
                MyUserDetails userDetails = (MyUserDetails)userDetailsService.loadUserByUsername(username);
                log.debug("JWT Filter - UserDetails loaded: {}", userDetails.getUsername());

                // 构建 Authentication 对象
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // 存入 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT Filter - Authentication set in SecurityContext");

                // 存入 UserContext
                UserContext userContext = userDetails.userContext();
                userContextHolder.setCurrentUser(userContext);
                
                // 验证 UserContext 是否设置成功
                UserContext verifyContext = userContextHolder.getCurrentUser();
                if (verifyContext != null && verifyContext.getId() != null) {
                    log.debug("JWT Filter - UserContext set successfully, userId: {}, username: {}", 
                            verifyContext.getId(), verifyContext.getUsername());
                } else {
                    log.error("JWT Filter - UserContext set failed! verifyContext: {}", verifyContext);
                }

                // 智能续期，当token快要过期时，如果用户还活跃，自动续期。
                if (jwtUtil.isNeedRenew(token)) {
                    String newToken = jwtUtil.generateAuthToken(userDetails.getUsername(),userDetails.userContext().getRole());

                    // 为了防止网络延迟等原因，redis过期时间比jwt失效时间多5分钟
                    tokenCacheService.saveToken(username, token,jwtUtil.getDefaultExpirationMinutes() + 5);
                    response.setHeader("X-New-Token", newToken);
                    log.debug("JWT Filter - Token renewed for user: {}", username);
                }
            } catch (Exception e) {
                log.error("JWT Filter - Error processing token: {}", e.getMessage(), e);
            }
        } else {
            if (token == null) {
                log.debug("JWT Filter - No token found in request header");
            } else {
                log.debug("JWT Filter - Token validation failed");
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * 验证jwt token是否过期和用户是否登出（如果用户登出了，remove redis中的token）
     * */
    private boolean isValidToken(String token) {
        return jwtUtil.isTokenValid(token) && tokenCacheService.isTokenValid(token);
    }
}
