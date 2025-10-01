package nus.iss.se.magicbag.entity;

import java.time.LocalDateTime;

public class CartItem {

    private Integer cartItemId;
    private Cart cart;
    private MagicBag magicBag;
    private int quantity;
    private LocalDateTime addedAt;
    private String status;

    public CartItem() {
        this.addedAt = LocalDateTime.now();
        this.status = "in_cart";
    }

    public Integer getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Integer cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public MagicBag getMagicBag() {
        return magicBag;
    }

    public void setMagicBag(MagicBag magicBag) {
        this.magicBag = magicBag;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
