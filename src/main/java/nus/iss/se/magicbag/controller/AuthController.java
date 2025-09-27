package nus.iss.se.magicbag.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.service.TokenCacheService;
import nus.iss.se.magicbag.auth.service.UserCacheService;
import nus.iss.se.magicbag.dto.LoginReq;
import nus.iss.se.magicbag.dto.RegisterReq;
import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.service.IUserService;
import nus.iss.se.magicbag.util.BaseUtil;
import nus.iss.se.magicbag.util.JwtUtil;
import nus.iss.se.magicbag.util.RsaUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "get public key, login, register...")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RsaUtil rsaUtil;
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
    public Result<Void> login(@RequestBody @Valid LoginReq request) {
        // 1. 认证（会调用 UserDetailsServiceImpl）
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        return Result.success();
    }

    @PostMapping("/logout")
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
}
