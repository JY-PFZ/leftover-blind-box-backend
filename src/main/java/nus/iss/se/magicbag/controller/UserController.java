package nus.iss.se.magicbag.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
public class UserController {

    private final IUserService userService;
    private final UserContextHolder userContextHolder;

    @GetMapping("/hello")
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

    @PostMapping("/profile")
    public Result<Void> update(UserDto user){
        userService.updateUserInfo(user);
        return Result.success();
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid RegisterReq req) {
        userService.register(req);
        return Result.success();
    }
}
