package nus.iss.se.magicbag.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("carts")
public class Cart {
    @TableId
    private Integer cartId;
    private Integer userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CartItem> cartItems = new ArrayList<>();

    public Cart() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getter / Setter
    public void addCartItem(CartItem cartItem) {
        cartItems.add(cartItem);
        cartItem.setCartId(this.cartId);
    }
    public void removeCartItem(CartItem cartItem) {
        cartItems.remove(cartItem);
        cartItem.setCartId(null);
    }
}
