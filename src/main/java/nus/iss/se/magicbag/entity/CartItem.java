package nus.iss.se.magicbag.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartItemId;  

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "magic_bag_id", nullable = false)
    private MagicBag magicBag;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @Column(name = "status", nullable = false)
    private String status; // in_cart / purchased

    public CartItem() {
        this.addedAt = LocalDateTime.now();
        this.status = "in_cart";
    }

    // Getter / Setter
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
