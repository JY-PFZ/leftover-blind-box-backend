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

    /**
     * **核心修复：这个新方法用于获取当前登录用户的个人资料**
     * 将 @GetMapping 注解从旧的 list() 方法移到这里
     */
    @GetMapping
    @Operation(summary = "Get current user's profile", description = "Fetches profile information for the currently logged-in user.")
    public Result<User> getCurrentUserProfile() {
        // 从 Spring Security 上下文中获取当前用户信息
        UserContext currentUserContext = userContextHolder.getCurrentUser();
        if (currentUserContext == null || currentUserContext.getUsername() == null) {
            // 如果安全上下文中没有用户信息，返回错误
            return Result.error("User not found in security context. Please log in again.");
        }

        // 使用用户名从数据库中查找完整的用户信息
        User user = userService.findByUsername(currentUserContext.getUsername());

        // 安全措施：在将用户信息发送到前端之前，清除密码字段
        if (user != null) {
            user.setPassword(null);
        }

        return Result.success(user);
    }

    /**
     * **核心修复：修改或移除旧的 list() 方法的路径，以避免冲突**
     * 我们可以给它一个新的路径，例如 /list，或者暂时注释掉
     */
    @GetMapping("/list") // 路径已修改为 /api/user/list
    public Result<IPage<User>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String role) {

        // 这里的逻辑仍然是空的，但它至少不再占用 /api/user 这个重要的路径
        IPage<User> page = null;
        return Result.success(page);
    }

    @PutMapping("/profile")
    @Operation(summary = "Edit user info", description = "Edit user info, but not include password")
    public Result<Void> update(@RequestBody @Valid UserDto userDto) {
        UserContext currentUser = userContextHolder.getCurrentUser();

        if (userDto.getId() == null) {
            userDto.setId(currentUser.getId());
        }

        userDto.setUsername(currentUser.getUsername());

        userService.updateUserInfo(userDto, currentUser);
        return Result.success();
    }

    @PostMapping("/register")
    @Operation(summary = "User register", description = "If user register, its account will be created with inactive status. Then an activate link will be sent to the user's email")
    public Result<String> register(@RequestBody @Valid RegisterReq req) {
        userService.register(req);
        return Result.success();
    }
}
