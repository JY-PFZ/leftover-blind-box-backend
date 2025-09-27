package nus.iss.se.magicbag.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.auth.service.UserCacheService;
import nus.iss.se.magicbag.dto.UserDto;
import nus.iss.se.magicbag.common.exception.BusinessException;
import nus.iss.se.magicbag.common.type.ResultStatus;
import nus.iss.se.magicbag.dto.RegisterReq;
import nus.iss.se.magicbag.entity.User;
import nus.iss.se.magicbag.mapper.UserMapper;
import nus.iss.se.magicbag.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService{
    private final PasswordEncoder passwordEncoder;
    private final UserCacheService userCacheService;

    @Override
    @Cacheable(value = "users", key = "#username", condition = "#username != null")
    public User findByUsername(String username) {
        return this.baseMapper.selectByUsername(username);
    }

    @Override
    public void register(RegisterReq req) {
        User existUser = this.baseMapper.selectByUsername(req.getUsername());
        if (existUser != null){
            throw new BusinessException(ResultStatus.USER_HAS_EXISTED, req.getUsername());
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        this.save(user);
    }

    @Override
    @CachePut(value = "users", key = "#dto.username")
    public void updateUserInfo(UserDto dto) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getUsername,dto.getUsername())
                .eq(User::getPhone,dto.getPhone())
                .set(User::getAvatar,dto.getAvatar())
                .set(User::getNickname,dto.getNickname());
        update(wrapper);

        // redis 更新
        User user = baseMapper.selectByUsername(dto.getUsername());
        UserContext userContext = new UserContext();
        BeanUtils.copyProperties(user,userContext);
        userCacheService.updateCache(userContext);
    }

    @Override
    @CacheEvict(value = "users", key = "#username")
    public void evictUser(String username) {
        log.info("Evict user: {}",username);
    }
}
