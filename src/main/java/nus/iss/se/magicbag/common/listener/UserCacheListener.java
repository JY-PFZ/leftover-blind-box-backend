package nus.iss.se.magicbag.common.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.UserContextHolder;
import nus.iss.se.magicbag.auth.UserInfo;
import nus.iss.se.magicbag.auth.service.UserCacheService;
import nus.iss.se.magicbag.common.event.UserInfoUpdatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserCacheListener {
    private final UserCacheService userCacheService;
    private final UserContextHolder userContextHolder;

    // 订阅UserUpdatedEvent事件
    @EventListener
    public void handleUserUpdatedEvent(UserInfoUpdatedEvent event) {
        UserInfo userInfo = event.getUserInfo();

        switch (event.getEventType()) {
            case NEW:
                userInfo.setLoginTime(new Date());
                userCacheService.updateCache(userInfo);
                userContextHolder.setCurrentUser(userInfo);
                log.debug("Save the user cache: {}", userInfo.getUsername());
            case UPDATE:
                userInfo.setLoginTime(userContextHolder.getCurrentUser().getLoginTime());
                userCacheService.updateCache(userInfo);
                userContextHolder.setCurrentUser(userInfo);
                log.debug("Update the user cache: {}", userInfo.getUsername());
                break;
            case DELETE:
                userCacheService.deleteUserCache(userInfo.getUsername());
                userContextHolder.clear();
                log.debug("Delete the user cache: {}", userInfo.getUsername());
                break;
        }
    }
}