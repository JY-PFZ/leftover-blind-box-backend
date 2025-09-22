package nus.iss.se.magicbag.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.auth.service.TokenCacheService;
import nus.iss.se.magicbag.util.BaseUtil;
import nus.iss.se.magicbag.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenCacheService tokenCacheService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        String token = BaseUtil.getTokenFromRequest(request);
        if (token != null && isValidToken(token)) {
            String username = jwtUtil.getClaims(token).getSubject();

            // 加载用户信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 构建 Authentication 对象
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // 存入 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 智能续期，当token快要过期时，如果用户还活跃，自动续期。
            if (jwtUtil.isNeedRenew(token)) {
                String newToken = jwtUtil.generateToken(userDetails.getUsername(),userDetails.getAuthorities());

                // 为了防止网络延迟等原因，redis过期时间比jwt失效时间多5分钟
                tokenCacheService.saveToken(username, token,jwtUtil.getDefaultExpirationMinutes() + 5);
                response.setHeader("X-New-Token", newToken);
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
