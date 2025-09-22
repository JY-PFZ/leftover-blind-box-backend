package nus.iss.se.magicbag.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.LoginUser;
import nus.iss.se.magicbag.auth.UserInfo;
import nus.iss.se.magicbag.entity.User;
import nus.iss.se.magicbag.exception.ResultEnum;
import nus.iss.se.magicbag.service.IUserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final IUserService userService;
    private final UserCacheService userCacheService;

    /**
     * 从redis或db获取user
     * */
    @Override
    public UserDetails loadUserByUsername(String username) {
        // 1. 先从redis获取
        UserInfo cachedUserInfo = userCacheService.getCachedUser(username);

        // 2.缓存未命中，查数据库
        if (cachedUserInfo == null){
            User user = userService.findByUsername(username);
            if (user == null){
                throw new UsernameNotFoundException(ResultEnum.USER_NOT_FOUND + ": " + username);
            }
            // 构造用户信息
            cachedUserInfo = UserInfo.build(user, new Date());
        }

        return new LoginUser(cachedUserInfo);
    }

    // 工具方法：判断当前请求是否为登录请求
//    private boolean isLoginRequest() {
//        try {
//            HttpServletRequest request = ((ServletRequestAttributes)
//                    RequestContextHolder.getRequestAttributes()).getRequest();
//            return "/api/auth/login".equals(request.getRequestURI())
//                    && "POST".equals(request.getMethod());
//        } catch (Exception e) {
//            return false;
//        }
//    }
}
