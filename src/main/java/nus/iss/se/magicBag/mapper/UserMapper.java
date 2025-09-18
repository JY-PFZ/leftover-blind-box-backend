package nus.iss.se.magicBag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.iss.se.magicBag.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT id, username, password, phone, nickname, avatar, created_at, updated_at, user_roles FROM users WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);
}
