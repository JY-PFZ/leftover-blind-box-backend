package nus.iss.se.magicbag.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.auth.common.UserContextHolder;
import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.dto.RegisterReq;
import nus.iss.se.magicbag.dto.UserDto;
import nus.iss.se.magicbag.entity.User;
import nus.iss.se.magicbag.service.IUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User API", description = "User service")
public class UserController {

    private final IUserService userService;
    private final UserContextHolder userContextHolder;

    @GetMapping("/hello")
    @Operation(summary = "ContextHolder test", description = "Test the user context holder whether can fetch the user info or not")
    public Result<String> hello(){
        UserContext currentUser = userContextHolder.getCurrentUser();
        return Result.success("hello spring security: " + currentUser.toString());
    }

    @GetMapping
    public Result<IPage<User>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String role) {

        IPage<User> page = null;
        return Result.success(page);
    }

    @PutMapping("/profile")
    @Operation(summary = "Edit user info", description = "Edit user info,but not include password")
    public Result<Void> update(@RequestBody @Valid UserDto userDto) {
        UserContext currentUser = userContextHolder.getCurrentUser();
        
        // 如果前端没有传递ID，使用当前用户ID
        if (userDto.getId() == null) {
            userDto.setId(currentUser.getId());
        }
        
        // 设置用户名（从当前用户上下文获取）
        userDto.setUsername(currentUser.getUsername());
        
        userService.updateUserInfo(userDto, currentUser);
        return Result.success();
    }

    @PostMapping("/register")
    @Operation(summary = "User register", description = "If user register, it's account will be create with inactive status. Then a activate link will send to user's email")
    public Result<String> register(@RequestBody @Valid RegisterReq req) {
        userService.register(req);
        return Result.success();
    }
}
