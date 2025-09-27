package nus.iss.se.magicbag.auth.service;

import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.auth.entity.MyUserDetails;
import nus.iss.se.magicbag.common.type.RedisPrefix;
import nus.iss.se.magicbag.util.RedisUtil;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 专门用于管理 JWT Token 在 Redis 中的状态
 */
@Service
@RequiredArgsConstructor
public class TokenCacheService {
    private final RedisUtil redisUtil;

    /**
     * 存储 Token（登录时调用）
     */
    public void saveToken(String username, String token, int minutes) {
        redisUtil.set(RedisPrefix.AUTH_TOKEN + token, username, minutes, TimeUnit.MINUTES);
    }

    public void saveUserInfo(MyUserDetails user){
        redisUtil.setJson(RedisPrefix.USER_INFO + user.getUsername(), user, 2,TimeUnit.HOURS);
    }

    /**
     * 检查 Token 是否有效（存在且未过期）
     */
    public boolean isTokenValid(String token) {
        return redisUtil.hasKey(RedisPrefix.AUTH_TOKEN + token);
    }

    /**
     * 撤销 Token（登出时调用）
     */
    public void revokeToken(String token) {
        redisUtil.delete(RedisPrefix.AUTH_TOKEN + token);
    }

    /**
     * 获取 Token 对应的用户名
     */
    public String getUsername(String token) {
        return redisUtil.get(RedisPrefix.AUTH_TOKEN + token);
    }
}