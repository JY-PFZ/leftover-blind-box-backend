package nus.iss.se.magicbag.mapper;

import org.apache.ibatis.annotations.*;
import nus.iss.se.magicbag.entity.Cart;

@Mapper
public interface CartMapper {

    @Insert("INSERT INTO carts (user_id, created_at, updated_at) VALUES (#{userId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "cartId")
    void insertCart(Cart cart);

    @Update("UPDATE carts SET updated_at = #{updatedAt} WHERE cart_id = #{cartId}")
    void updateCart(Cart cart);

    @Select("SELECT * FROM carts WHERE user_id = #{userId}")
    Cart findByUserId(@Param("userId") Integer userId);
}
