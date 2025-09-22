package nus.iss.se.magicbag.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.dto.Result;
import nus.iss.se.magicbag.entity.User;
import nus.iss.se.magicbag.service.IUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping("/hello")
    public Result<String> hello(){
        return Result.success("hello spring security");
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
    public Result<User> update(User user){
        User updatedUser = userService.updateUserInfo(user);
        return Result.success(updatedUser);
    }
}
