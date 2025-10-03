package nus.iss.se.magicbag.mapper;

import org.apache.ibatis.annotations.*;
import nus.iss.se.magicbag.entity.CartItem;
import java.util.List;

@Mapper
public interface CartItemMapper {

    @Insert("INSERT INTO cart_items (cart_id, magic_bag_id, quantity, added_at, status) " +
            "VALUES (#{cartId}, #{magicBagId}, #{quantity}, #{addedAt}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "cartItemId")
    void insertCartItem(CartItem cartItem);

    @Update("UPDATE cart_items SET quantity=#{quantity}, added_at=#{addedAt} WHERE cart_item_id=#{cartItemId}")
    void updateCartItem(CartItem cartItem);

    @Delete("DELETE FROM cart_items WHERE cart_item_id=#{cartItemId}")
    void deleteCartItem(@Param("cartItemId") Integer cartItemId);

    @Select("SELECT * FROM cart_items WHERE cart_id=#{cartId} AND magic_bag_id=#{magicBagId} LIMIT 1")
    CartItem findByCartIdAndMagicBagId(@Param("cartId") Integer cartId, @Param("magicBagId") Integer magicBagId);

    @Select("SELECT * FROM cart_items WHERE cart_id=#{cartId}")
    List<CartItem> findByCartId(@Param("cartId") Integer cartId);

    @Delete("DELETE FROM cart_items WHERE cart_id=#{cartId}")
    void deleteByCartId(@Param("cartId") Integer cartId);
    
    @Select("SELECT * FROM cart_items WHERE magic_bag_id=#{magicBagId}")
    List<CartItem> findByMagicBagId(@Param("magicBagId") Integer magicBagId);
}
