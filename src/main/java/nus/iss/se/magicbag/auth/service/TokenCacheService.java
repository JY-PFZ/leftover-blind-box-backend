package nus.iss.se.magicbag.auth.service;

import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.common.constant.RedisPrefix;
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

    private String getAuthUserKey(String username){
        return RedisPrefix.AUTH_USER.getCode() + username;
    }

    private String getAuthTokenKey(String token){
        return RedisPrefix.AUTH_TOKEN.getCode() + token;
    }

    /**
     * 存储 Token（登录时调用）
     */
    public void saveToken(String username, String token, int minutes) {
        // 如果之前用户登陆过，先清空
        revokeTokenByUsername(username);
        redisUtil.set(getAuthTokenKey(token), username, minutes, TimeUnit.MINUTES);
        redisUtil.set(getAuthUserKey(username), token, minutes, TimeUnit.MINUTES);
    }

    /**
     * 检查 Token 是否有效（存在且未过期）
     */
    public boolean isTokenValid(String token) {
        return redisUtil.hasKey(getAuthTokenKey(token));
    }

    /**
     * 撤销 Token（登出时调用）
     */
    public void revokeToken(String token) {
        redisUtil.delete(getAuthTokenKey(token));
    }

    public void revokeTokenByUsername(String username){
        String token = redisUtil.get(getAuthUserKey(username));
        if (token != null){
            redisUtil.delete(getAuthTokenKey(token));
            redisUtil.delete(getAuthUserKey(username));
        }
    }

    /**
     * 获取 Token 对应的用户名
     */
    public String getUsername(String token) {
        return redisUtil.get(RedisPrefix.AUTH_TOKEN.getCode() + token);
    }
}