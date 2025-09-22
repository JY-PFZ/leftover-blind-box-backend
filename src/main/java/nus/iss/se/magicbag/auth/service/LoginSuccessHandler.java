package nus.iss.se.magicbag.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.auth.LoginUser;
import nus.iss.se.magicbag.common.event.UserInfoUpdatedEvent;
import nus.iss.se.magicbag.dto.Result;
import nus.iss.se.magicbag.util.JwtUtil;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 1. 获取user信息
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        // 2. 生成 JWT 令牌
        String token = jwtUtil.generateToken(loginUser.getUsername(),loginUser.getAuthorities());

        // 3.把token放到redis中
        tokenCacheService.saveToken(loginUser.getUsername(), token, jwtUtil.getDefaultExpirationMinutes() + 5);

        // 4. 发布事件，把用户信息发到用户上下文和redis中
        eventPublisher.publishEvent(new UserInfoUpdatedEvent(this, loginUser.getUserInfo(), UserInfoUpdatedEvent.EventType.NEW));

        // 5. 返回 Token 给前端
        response.setHeader("X-New-Token", token);

        String json = objectMapper.writeValueAsString(Result.success());
        response.getWriter().write(json);
    }
}
