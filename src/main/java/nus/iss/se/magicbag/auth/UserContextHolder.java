package nus.iss.se.magicbag.auth;

import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.entity.User;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 当前用户上下文，每个请求独立实例
 * */
@Data
@Component
@RequiredArgsConstructor
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserContextHolder {
    private UserInfo currentUser;
    private String token;

    /**
     * 清除上下文
     * */
    @PreDestroy
    public void clear(){
        currentUser = null;
        token = null;
    }

    public void updateUserInfo(User user){
        // TODO 更新用户信息
        currentUser.setAvatar(user.getAvatar());
    }

    public UserInfo getCurrentUser() {
        if (currentUser == null) {
            // 从安全上下文同步（可选，作为兜底）
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                currentUser = loginUser.getUserInfo();
            }
        }
        return currentUser;
    }
}
