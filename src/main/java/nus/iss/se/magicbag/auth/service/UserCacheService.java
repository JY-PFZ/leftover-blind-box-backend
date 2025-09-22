package nus.iss.se.magicbag.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.UserInfo;
import nus.iss.se.magicbag.common.RedisKeyPrefix;
import nus.iss.se.magicbag.util.RedisUtil;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheService {
    private final RedisUtil redisUtil;
    private static final Duration USER_CACHE_TTL = Duration.ofHours(2);
    private static final Duration RENEW_THRESHOLD = Duration.ofMinutes(30);

    public UserInfo getCachedUser(String username) {
        String key = getCacheKey(username);
        UserInfo cachedUserInfo = redisUtil.getJson(key, UserInfo.class);
        if (cachedUserInfo != null){
            // 缓存命中后， 判断是否需要续期，当生命周期小于30min时自动续期
            if (redisUtil.getExpire(key,TimeUnit.SECONDS) < RENEW_THRESHOLD.getSeconds()){
                redisUtil.expire(key, USER_CACHE_TTL.getSeconds(), TimeUnit.SECONDS);
                log.debug("Redis User cache renewed for: {}",username);
            }
        }
        return cachedUserInfo;
    }

    public void updateCache(UserInfo userInfo) {
        cacheUser(userInfo);
    }

    public void cacheUser(UserInfo userInfo) {
        // userInfo中password注解实现移除敏感信息，缓存
        redisUtil.setJson(getCacheKey(userInfo.getUsername()),userInfo,USER_CACHE_TTL.getSeconds(), TimeUnit.SECONDS);
    }

    /**
     * 清除user缓存
     * */
    public void deleteUserCache(String username) {
        redisUtil.delete(getCacheKey(username));
    }

    private String getCacheKey(String username) {
        return RedisKeyPrefix.USER_INFO + username;
    }
}
