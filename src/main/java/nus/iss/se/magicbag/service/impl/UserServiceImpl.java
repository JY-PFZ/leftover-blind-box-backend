package nus.iss.se.magicbag.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.auth.UserInfo;
import nus.iss.se.magicbag.common.event.UserInfoUpdatedEvent;
import nus.iss.se.magicbag.exception.BusinessException;
import nus.iss.se.magicbag.exception.ResultEnum;
import nus.iss.se.magicbag.dto.RegisterReq;
import nus.iss.se.magicbag.entity.User;
import nus.iss.se.magicbag.mapper.UserMapper;
import nus.iss.se.magicbag.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService{
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public User findByUsername(String username) {
        return this.baseMapper.selectByUsername(username);
    }

    @Override
    public void register(RegisterReq req) {
        User existUser = findByUsername(req.getUsername());
        if (existUser != null){
            throw new BusinessException(ResultEnum.USER_HAS_EXISTED, req.getUsername());
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        this.save(user);
    }

    @Override
    public User updateUserInfo(User dto) {
        if (StringUtils.isBlank(dto.getUsername())){
            throw new BusinessException(ResultEnum.USER_NOT_FOUND, "username is null or blank");
        }
        User user = findByUsername(dto.getUsername());
        user.setAvatar(dto.getAvatar());
        user.setNickname(dto.getNickname());
        updateById(user);

        // 发布“用户更新事件”
        eventPublisher.publishEvent(
                new UserInfoUpdatedEvent(this, UserInfo.build(user), UserInfoUpdatedEvent.EventType.UPDATE)
        );
        return user;
    }
}
