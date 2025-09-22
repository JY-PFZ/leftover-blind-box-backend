package nus.iss.se.magicbag.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
@TableName("users")
public class User {
    @TableId
    private Integer id;
    private String username;
    private String password;
    private String role;
    private String phone;
    private String nickname;
    private String avatar;
    private Date createdAt;
    private Date updatedAt;
}
