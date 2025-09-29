package nus.iss.se.magicbag.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import nus.iss.se.magicbag.entity.Cart;
import nus.iss.se.magicbag.entity.CartItem;
import nus.iss.se.magicbag.service.impl.CartImpl;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart API", description = "Cart")
public class CartController {

    @Autowired
    private CartImpl cartService;

    @PostMapping("/create/{userId}")
    @Operation(summary = "Create cart", description = "Create cart for those new users")
    public Cart createCart(
            @Parameter(description = "userId") @PathVariable Integer userId) {
        return cartService.createCart(userId);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user cart", description = "Get current user cart with userId")
    public Cart getCart(
            @Parameter(description = "userId") @PathVariable Integer userId) {
        return cartService.getActiveCart(userId);
    }

    @PostMapping("/{userId}/add")
    @Operation(summary = "Add magicbag", description = "Add specific magicbag into user's cart")
    public Cart addItem(
            @Parameter(description = "userId") @PathVariable Integer userId,
            @Parameter(description = "magicId") @RequestParam Integer magicbagId,
            @Parameter(description = "quantity") @RequestParam int quantity) {
        return cartService.addItemToCart(userId, magicbagId, quantity);
    }

    @PutMapping("/{userId}/update")
    @Operation(summary = "Update", description = "Modify quantity of specific magicbag in the cart")
    public Cart updateItem(
            @Parameter(description = "userId") @PathVariable Integer userId,
            @Parameter(description = "magicbagId") @RequestParam Integer magicbagId,
            @Parameter(description = "new quantity") @RequestParam int newQuantity) {
        return cartService.updateItemQuantityInCart(userId, magicbagId, newQuantity);
    }

    @DeleteMapping("/{userId}/remove")
    @Operation(summary = "Remove Magicbag", description = "Remove specific magicbag from user's cart")
    public Cart removeItem(
            @Parameter(description = "userId") @PathVariable Integer userId,
            @Parameter(description = "magicbagId") @RequestParam Integer magicbagId) {
        return cartService.removeItemFromCart(userId, magicbagId);
    }

    @GetMapping("/{userId}/items")
    @Operation(summary = "Cart items list", description = "Get all the magicbags in the cart")
    public List<CartItem> getCartItems(
            @Parameter(description = "userId") @PathVariable Integer userId) {
        return cartService.getCartItems(userId);
    }

    @DeleteMapping("/{userId}/clear")
    @Operation(summary = "Clear the Cart", description = "Delete all the magicbags in the cart")
    public Cart clearCart(
            @Parameter(description = "userId") @PathVariable Integer userId) {
        return cartService.clearCart(userId);
    }

    @GetMapping("/{userId}/total")
    @Operation(summary = "Get total", description = "Compute the total price in the cart")
    public double getTotal(
            @Parameter(description = "userId") @PathVariable Integer userId) {
        return cartService.getTotal(userId);
    }
}
