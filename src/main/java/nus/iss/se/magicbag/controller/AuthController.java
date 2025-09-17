package nus.iss.se.magicbag.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.dto.LoginReq;
import nus.iss.se.magicbag.dto.LoginResp;
import nus.iss.se.magicbag.dto.Result;
import nus.iss.se.magicbag.service.IUserService;
import nus.iss.se.magicbag.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final IUserService userService;

    @PostMapping("/login")
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
