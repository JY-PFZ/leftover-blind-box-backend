package nus.iss.se.magicbag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.dto.RegisterReq;
import nus.iss.se.magicbag.dto.UserDto;
import nus.iss.se.magicbag.entity.User;

public interface IUserService extends IService<User> {
    User findByUsername(String username);

    void register(RegisterReq req);

    void activateUser(String username);

    void updateUserInfo(UserDto user);

    /**
     * 更新用户个人信息 （带权限验证） 
     * @param dto 用户更新信息
     * @param currentUser 当前登录用户上下文
     */
    void updateUserInfo(UserDto dto, UserContext currentUser);

    void evictUser(String username);
}
