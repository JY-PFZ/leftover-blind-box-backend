package nus.iss.se.magicbag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import nus.iss.se.magicbag.dto.LoginReq;
import nus.iss.se.magicbag.dto.RegisterReq;
import nus.iss.se.magicbag.entity.User;

public interface IUserService extends IService<User> {
    User findByUsername(String username);

    void register(RegisterReq req);

    User updateUserInfo(User user);
}
