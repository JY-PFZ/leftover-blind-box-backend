package nus.iss.se.magicBag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import nus.iss.se.magicBag.dto.LoginReq;
import nus.iss.se.magicBag.entity.User;

public interface IUserService extends IService<User> {
    User findByUsername(String username);

    boolean register(LoginReq req);
}
