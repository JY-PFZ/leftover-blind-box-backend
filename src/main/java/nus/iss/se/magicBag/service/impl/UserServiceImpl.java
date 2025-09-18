package nus.iss.se.magicBag.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicBag.common.ResultEnum;
import nus.iss.se.magicBag.dto.LoginReq;
import nus.iss.se.magicBag.entity.User;
import nus.iss.se.magicBag.exception.UserErrorException;
import nus.iss.se.magicBag.mapper.UserMapper;
import nus.iss.se.magicBag.service.IUserService;
import nus.iss.se.magicBag.util.PasswordUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService{
    private final PasswordUtil passwordUtil;

    @Override
    public User findByUsername(String username) {
        return this.baseMapper.selectByUsername(username);
    }

    @Override
    public boolean register(LoginReq req) {
        User existUser = findByUsername(req.getUsername());
        if (existUser != null){
            throw new UserErrorException(ResultEnum.USER_HAS_EXISTED);
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordUtil.encode(req.getPassword()));
        return this.save(user);
    }
}
