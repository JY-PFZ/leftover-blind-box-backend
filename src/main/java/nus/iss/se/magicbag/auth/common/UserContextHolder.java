package nus.iss.se.magicbag.auth.common;

import jakarta.annotation.PreDestroy;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * 当前用户上下文，每个请求独立实例
 * */
@Data
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserContextHolder {
    private UserContext currentUser;
    private String token;

    /**
     * 清除上下文
     * */
    @PreDestroy
    public void clear(){
        currentUser = null;
        token = null;
    }
}
