package nus.iss.se.magicBag.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicBag.dto.LoginReq;
import nus.iss.se.magicBag.dto.LoginResp;
import nus.iss.se.magicBag.dto.Result;
import nus.iss.se.magicBag.service.IUserService;
import nus.iss.se.magicBag.util.JwtUtil;
import nus.iss.se.magicBag.util.RsaUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

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
    public Result<LoginResp> login(@RequestBody @Valid LoginReq request) {
        // 1. 认证（会调用 UserDetailsServiceImpl）
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. 生成 JWT Token
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        role = role.replace("ROLE_", "").toLowerCase();

        String token = jwtUtil.generateToken(request.getUsername(), role);

        // 3. 返回结果
        LoginResp resp = new LoginResp(request.getUsername(), token,role,new Date());
        log.info("user login: {}",resp);
        return Result.success(resp);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid LoginReq req){
        userService.register(req);
        return Result.success();
    }
}
