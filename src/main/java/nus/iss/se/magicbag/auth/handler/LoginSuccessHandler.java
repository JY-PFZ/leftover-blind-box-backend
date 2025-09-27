package nus.iss.se.magicbag.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.auth.entity.MyUserDetails;
import nus.iss.se.magicbag.auth.service.TokenCacheService;
import nus.iss.se.magicbag.auth.service.UserCacheService;
import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final TokenCacheService tokenCacheService;
    private final UserCacheService userCacheService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 1. 获取user信息
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();

        // 2. 生成 JWT 令牌
        String token = jwtUtil.generateToken(myUserDetails.getUsername(), myUserDetails.getAuthorities());

        // 3.把token放到redis中
        tokenCacheService.saveToken(myUserDetails.getUsername(), token, jwtUtil.getDefaultExpirationMinutes() + 5);

        // 4. 发布事件，把用户信息缓存到redis中
        userCacheService.cacheUser(myUserDetails.userContext());

        // 5. 返回 Token 给前端
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("X-New-Token", token);
        response.setStatus(HttpStatus.OK.value());

        String json = objectMapper.writeValueAsString(Result.success());
        response.getWriter().write(json);
    }
}
