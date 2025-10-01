package nus.iss.se.magicbag.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("cart_items")
public class CartItem {
    @TableId
    private Integer cartItemId;
    private Integer cartId;
    private Integer magicBagId;
    private Integer quantity;
    private LocalDateTime addedAt;
    private String status; // in_cart / purchased

    public CartItem() {
        this.addedAt = LocalDateTime.now();
        this.status = "in_cart";
    }

    // Getter / Setter
    public Integer getCartItemId() { return cartItemId; }
    public void setCartItemId(Integer cartItemId) { this.cartItemId = cartItemId; }
    public Integer getCartId() { return cartId; }
    public void setCartId(Integer cartId) { this.cartId = cartId; }
    public Integer getMagicBagId() { return magicBagId; }
    public void setMagicBagId(Integer magicBagId) { this.magicBagId = magicBagId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
