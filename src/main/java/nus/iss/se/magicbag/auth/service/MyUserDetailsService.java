package nus.iss.se.magicbag.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.auth.entity.MyUserDetails;
import nus.iss.se.magicbag.auth.common.UserContext;
import nus.iss.se.magicbag.entity.User;
import nus.iss.se.magicbag.common.type.ResultStatus;
import nus.iss.se.magicbag.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final IUserService userService;

    /**
     * 从redis或db获取user
     * */
    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userService.findByUsername(username);
        if (user == null){
            throw new UsernameNotFoundException(ResultStatus.USER_NOT_FOUND + ": " + username);
        }
        // 构造用户信息
        UserContext userContext = new UserContext();
        BeanUtils.copyProperties(user,userContext);
        return new MyUserDetails(userContext,user.getPassword());
    }
}
