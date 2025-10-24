package nus.iss.se.magicbag.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.auth.service.UserCacheService;
import nus.iss.se.magicbag.common.constant.UserStatus;
import nus.iss.se.magicbag.dto.UserDto;
import nus.iss.se.magicbag.common.exception.BusinessException;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import nus.iss.se.magicbag.dto.RegisterReq;
import nus.iss.se.magicbag.entity.User;
import nus.iss.se.magicbag.mapper.UserMapper;
import nus.iss.se.magicbag.service.EmailService;
import nus.iss.se.magicbag.service.IUserService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService{
    private final PasswordEncoder passwordEncoder;
    private final UserCacheService userCacheService;
    private final EmailService emailService;

    @Override
    @Cacheable(value = "users", key = "#username", condition = "#username != null")
    public User findByUsername(String username) {
        return this.baseMapper.selectByUsername(username);
    }

    @Override
    @Transactional
    public void register(RegisterReq req) {
        User existUser = this.baseMapper.selectByUsername(req.getUsername());
        if (existUser != null){
            throw new BusinessException(ResultStatus.USER_HAS_EXISTED, req.getUsername());
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setRole(req.getRole());
        user.setStatus(UserStatus.INACTIVE.getCode());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        this.save(user);

        try {
            emailService.sendActivationEmail(req.getUsername());
        } catch (MessagingException e) {
            log.error("Send account activate mail failed: {}->{}",req.getUsername(), ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#dto.username")
    public void updateUserInfo(UserDto dto) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getUsername,dto.getUsername())
                .eq(User::getPhone,dto.getPhone())
                .set(User::getAvatar,dto.getAvatar())
                .set(User::getNickname,dto.getNickname());
        update(wrapper);

        User user = baseMapper.selectByUsername(dto.getUsername());
        UserContext userContext = new UserContext();
        BeanUtils.copyProperties(user,userContext);
        userCacheService.updateCache(userContext);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#username")
    public void activateUser(String username) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getUsername,username)
                .eq(User::getStatus, UserStatus.INACTIVE.getCode())
                .set(User::getStatus, UserStatus.ACTIVE.getCode());
        update(wrapper);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#currentUser.username")
    public void updateUserInfo(UserDto dto, UserContext currentUser) {
        // 1. 权限验证
        if (!Objects.equals(currentUser.getId(), dto.getId())) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED, "无权限修改他人信息");
        }

        // **核心修复：将 "CUSTOMER" 添加为合法的角色**
        String userRole = currentUser.getRole();
        if (!"CUSTOMER".equals(userRole) && !"MERCHANT".equals(userRole)) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED, "角色权限不足");
        }

        // 3. 手机号唯一性验证
        if (StringUtils.hasText(dto.getPhone())) {
            User existingUser = baseMapper.selectByPhone(dto.getPhone());
            if (existingUser != null && !Objects.equals(existingUser.getId(), dto.getId())) {
                throw new BusinessException(ResultStatus.USER_HAS_EXISTED, "手机号已被其他用户使用");
            }
        }

        // 4. 更新用户信息
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, dto.getId());

        if (StringUtils.hasText(dto.getNickname())) {
            wrapper.set(User::getNickname, dto.getNickname());
        }
        if (StringUtils.hasText(dto.getAvatar())) {
            wrapper.set(User::getAvatar, dto.getAvatar());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            wrapper.set(User::getPhone, dto.getPhone());
        }

        wrapper.set(User::getUpdatedAt, new Date());

        boolean updated = update(wrapper);
        if (!updated) {
            throw new BusinessException(ResultStatus.FAIL, "用户信息更新失败");
        }

        // 5. 更新缓存
        User updatedUser = baseMapper.selectById(dto.getId());
        UserContext userContext = new UserContext();
        BeanUtils.copyProperties(updatedUser, userContext);
        userCacheService.updateCache(userContext);

        log.info("用户 {} 更新个人信息成功", currentUser.getUsername());
    }

    @Override
    @CacheEvict(value = "users", key = "#username")
    public void evictUser(String username) {
        log.info("Evict user: {}",username);
    }
}

