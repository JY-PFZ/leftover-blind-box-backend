package nus.iss.se.magicbag.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nus.iss.se.magicbag.entity.User;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    private Integer id;
    private String username;

    @JsonIgnore
    private String password;
    private String role;
    private String phone;
    private String nickname;
    private String avatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date loginTime;

    public static UserInfo build(User user,Date loginTime){
        UserInfo userInfo = build(user);
        userInfo.setLoginTime(loginTime);
        return userInfo;
    }

    public static UserInfo build(User user){
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(user,userInfo);
        return userInfo;
    }
}
