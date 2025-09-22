package nus.iss.se.magicbag.common.event;

import lombok.Getter;
import nus.iss.se.magicbag.auth.UserInfo;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserInfoUpdatedEvent extends ApplicationEvent {
    private final UserInfo userInfo;
    private final EventType eventType;

    public UserInfoUpdatedEvent(Object source, UserInfo userInfo, EventType eventType) {
        super(source);
        this.userInfo = userInfo;
        this.eventType = eventType;
    }

    // 事件类型：更新/删除
    public enum EventType {
        NEW, UPDATE, DELETE
    }
}
