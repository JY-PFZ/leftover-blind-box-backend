package nus.iss.se.magicbag.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.common.constant.RedisPrefix;
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

    public UserContext getCachedUser(String username) {
        String key = getCacheKey(username);
        UserContext cachedUserContext = redisUtil.getJson(key, UserContext.class);
        // 缓存命中后， 判断是否需要续期，当生命周期小于30min时自动续期
        if (cachedUserContext != null && redisUtil.getExpire(key,TimeUnit.SECONDS) < RENEW_THRESHOLD.getSeconds()){
            redisUtil.expire(key, USER_CACHE_TTL.getSeconds(), TimeUnit.SECONDS);
            log.debug("Redis User cache renewed for: {}",username);
        }
        return cachedUserContext;
    }

    public void updateCache(UserContext userContext) {
        cacheUser(userContext);
    }

    public void cacheUser(UserContext userContext) {
        // userInfo中password注解实现移除敏感信息，缓存
        redisUtil.setJson(getCacheKey(userContext.getUsername()), userContext,USER_CACHE_TTL.getSeconds(), TimeUnit.SECONDS);
    }

    /**
     * 清除user缓存
     * */
    public void deleteUserCache(String username) {
        redisUtil.delete(getCacheKey(username));
    }

    private String getCacheKey(String username) {
        return RedisPrefix.USER_INFO.getCode() + username;
    }
}
