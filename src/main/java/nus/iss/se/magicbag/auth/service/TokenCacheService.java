package nus.iss.se.magicbag.auth.service;

import nus.iss.se.magicbag.auth.LoginUser;
import nus.iss.se.magicbag.common.RedisKeyPrefix;
import nus.iss.se.magicbag.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 专门用于管理 JWT Token 在 Redis 中的状态
 */
@Service
public class TokenCacheService {
    private final RedisUtil redisUtil;

    @Autowired
    public TokenCacheService(RedisUtil RedisUtil) {
        this.redisUtil = RedisUtil;

    }

    /**
     * 存储 Token（登录时调用）
     */
    public void saveToken(String username, String token, int minutes) {
        redisUtil.set(RedisKeyPrefix.AUTH_TOKEN + token, username, minutes, TimeUnit.MINUTES);
    }

    public void saveUserInfo(LoginUser user){
        redisUtil.setJson(RedisKeyPrefix.USER_INFO + user.getUsername(), user, 2,TimeUnit.HOURS);
    }

    /**
     * 检查 Token 是否有效（存在且未过期）
     */
    public boolean isTokenValid(String token) {
        return redisUtil.hasKey(RedisKeyPrefix.AUTH_TOKEN + token);
    }

    /**
     * 撤销 Token（登出时调用）
     */
    public void revokeToken(String token) {
        redisUtil.delete(RedisKeyPrefix.AUTH_TOKEN + token);
    }

    /**
     * 获取 Token 对应的用户名
     */
    public String getUsername(String token) {
        return redisUtil.get(RedisKeyPrefix.AUTH_TOKEN + token);
    }
}