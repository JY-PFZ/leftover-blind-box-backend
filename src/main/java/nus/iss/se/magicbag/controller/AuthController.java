package nus.iss.se.magicbag.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.entity.MyUserDetails;
import nus.iss.se.magicbag.auth.service.TokenCacheService;
import nus.iss.se.magicbag.auth.service.UserCacheService;
import nus.iss.se.magicbag.common.constant.RedisPrefix;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import nus.iss.se.magicbag.dto.LoginReq;
import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.service.EmailService;
import nus.iss.se.magicbag.service.IUserService;
import nus.iss.se.magicbag.util.BaseUtil;
import nus.iss.se.magicbag.util.JwtUtil;
import nus.iss.se.magicbag.util.RedisUtil;
import nus.iss.se.magicbag.util.RsaUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "Get rsa public key, login, activate account")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RsaUtil rsaUtil;
    private final RedisUtil redisUtil;
    private final EmailService emailService;
    private final IUserService userService;
    private final TokenCacheService tokenCacheService;
    private final UserCacheService userCacheService;

    @GetMapping("/key")
    @Operation(summary = "Obtain the RSA public key", description = "Return the Pem of the RSA public key for front-end encryption")
    @ApiResponse(responseCode = "200", description = "Successfully returned the public key",
            content = @Content(schema = @Schema(implementation = Result.class)))
    public Result<String> getPublicKey(){
        return Result.success(rsaUtil.getPublicKeyAsPem());
    }

    @PostMapping("/login")
    @Operation(summary = "user login", description = "Submit after encrypting the password with RSA")
    @ApiResponse(responseCode = "200", description = "login success")
    @ApiResponse(responseCode = "400", description = "login fail")
    public Result<Void> login(HttpServletResponse response, @RequestBody @Valid LoginReq loginReq) {
        // 1. 认证（会调用 UserDetailsServiceImpl）
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginReq.getUsername(), loginReq.getPassword())
        );
        // 存入 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. 获取user信息
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();

        // 3. 生成 JWT 令牌
        String token = jwtUtil.generateAuthToken(userDetails.getUsername(), userDetails.userContext().getRole());

        // 4.把token放到redis中
        tokenCacheService.saveToken(userDetails.getUsername(), token, jwtUtil.getDefaultExpirationMinutes() + 5);

        // 5. 发布事件，把用户信息存到redis中
        userCacheService.cacheUser(userDetails.userContext());

        // 6. 返回 Token 给前端
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("X-New-Token", token);

        return Result.success();
    }

    @PostMapping("/logout")
    @Operation(summary = "user logout", description = "Delete user's token on redis")
    public Result<String> logout(HttpServletRequest request) {
        String token = BaseUtil.getTokenFromRequest(request);
        if (token != null) {
            // 从 JWT 解析用户名
            String username = jwtUtil.getClaims(token).getSubject();

            //  1. Redis 标记 token 失效
            tokenCacheService.revokeToken(token);

            // 2. 清除用户信息上下文和redis缓存
            userCacheService.deleteUserCache(username);
            userService.evictUser(username);
            log.info("User logout: {}", username);
        }
        return Result.success();
    }

    @GetMapping("/activate")
    @Operation(summary = "Activate User Account", description = "Verify the token which is stored in the redis and mapped to a user account. if verified success, change the account status to active")
    public Result<Void> activate(@RequestParam String token){
        String key = RedisPrefix.ACCOUNT_ACTIVATE_TOKEN.getCode() + token;
        String username = redisUtil.get(key);
        if (username == null){
            return Result.error(ResultStatus.USER_ACTIVATE_TOKEN_EXPIRE);
        }

        // 激活用户
        userService.activateUser(username);
        // 移除token
        redisUtil.delete(key);
        return Result.success();
    }

    @PostMapping("/activate/resend/{username}")
    @Operation(summary = "resend activate link", description = "Re-enter account and password. After verification is successful, an activation link will be sent.")
    public Result<Void> resendActivateMail(@PathVariable String username) throws MessagingException {
        emailService.sendActivationEmail(username);
        return Result.success();
    }
}
