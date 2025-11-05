package nus.iss.se.magicbag.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.common.UserContextHolder;
import nus.iss.se.magicbag.auth.entity.MyUserDetails;
import nus.iss.se.magicbag.auth.service.TokenCacheService;
import nus.iss.se.magicbag.util.BaseUtil;
import nus.iss.se.magicbag.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
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
                userContextHolder.setCurrentUser(userDetails.userContext());
                log.debug("JWT Filter - UserContext set, userId: {}", userDetails.userContext().getId());

                // 智能续期，当token快要过期时，如果用户还活跃，自动续期。
                if (jwtUtil.isNeedRenew(token)) {
                    String newToken = jwtUtil.generateAuthToken(userDetails.getUsername(),userDetails.userContext().getRole());

                    // 为了防止网络延迟等原因，redis过期时间比jwt失效时间多5分钟
                    tokenCacheService.saveToken(username, token,jwtUtil.getDefaultExpirationMinutes() + 5);
                    response.setHeader("X-New-Token", newToken);
                    log.debug("JWT Filter - Token renewed");
                }
            } catch (Exception e) {
                log.error("JWT Filter - Error processing token: {}", e.getMessage(), e);
            }
        } else {
            if (token == null) {
                log.debug("JWT Filter - No token found in request");
            } else {
                log.warn("JWT Filter - Token invalid or not found in Redis: {}", token.substring(0, Math.min(20, token.length())));
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * 验证jwt token是否过期和用户是否登出（如果用户登出了，remove redis中的token）
     * */
    private boolean isValidToken(String token) {
        boolean jwtValid = jwtUtil.isTokenValid(token);
        boolean redisValid = tokenCacheService.isTokenValid(token);
        
        log.debug("JWT Filter - Token validation: JWT valid={}, Redis valid={}", jwtValid, redisValid);
        
        // 如果 JWT 有效但 Redis 中没有，可能是 Redis 连接问题或 token 过期
        // 为了更好的用户体验，如果 JWT 本身有效，可以允许通过（但需要重新保存到 Redis）
        if (jwtValid && !redisValid) {
            log.warn("JWT Filter - Token is valid but not in Redis, attempting to restore");
            try {
                String username = jwtUtil.getClaims(token).getSubject();
                // 重新保存 token 到 Redis
                tokenCacheService.saveToken(username, token, jwtUtil.getDefaultExpirationMinutes() + 5);
                log.info("JWT Filter - Token restored to Redis for user: {}", username);
                return true;
            } catch (Exception e) {
                log.error("JWT Filter - Failed to restore token to Redis: {}", e.getMessage());
                return false;
            }
        }
        
        return jwtValid && redisValid;
    }
}
