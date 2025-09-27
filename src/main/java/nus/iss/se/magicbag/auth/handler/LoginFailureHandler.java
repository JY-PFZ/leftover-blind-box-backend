package nus.iss.se.magicbag.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.common.type.ResultStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        // 提取用户名（从请求参数或异常中）
        String username = request.getParameter("username");
        log.info("user login failed: {}",username);
        // 返回错误信息
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String message = exception.getMessage();
        if (exception instanceof BadCredentialsException){
            message = "Invalid username or password";
        }
        String json = objectMapper.writeValueAsString(Result.error(ResultStatus.FAIL,message));
        response.getWriter().write(json);
    }
}
