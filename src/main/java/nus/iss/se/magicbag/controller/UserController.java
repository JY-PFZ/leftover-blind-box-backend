package nus.iss.se.magicbag.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.entity.User;
import nus.iss.se.magicbag.service.IUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        return ResponseEntity.ok("hello spring security");
    }

    @GetMapping
    public ResponseEntity<IPage<User>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String role) {

        IPage<User> page = null;
        return ResponseEntity.ok(page);
    }
}
