package nus.iss.se.magicbag.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.dto.CartDto;
import nus.iss.se.magicbag.dto.CartItemDto;
import nus.iss.se.magicbag.service.impl.CartServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart API", description = "APIs to manage user shopping cart")
@RequiredArgsConstructor
public class CartController {
    private final CartServiceImpl cartService;

    @PostMapping("/{userId}")
    @Operation(summary = "Create a new cart for user", description = "Creates a new shopping cart for the given user ID")
    public CartDto createCart(@PathVariable Integer userId) {
        return cartService.createCart(userId);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get active cart for user", description = "Retrieve the currently active shopping cart for a user")
    public CartDto getCart(@PathVariable Integer userId) {
        return cartService.getActiveCart(userId);
    }

    @PostMapping("/{userId}/items")
    @Operation(summary = "Add item to cart", description = "Add a MagicBag item to the user's shopping cart with specified quantity")
    public CartDto addItemToCart(@PathVariable Integer userId,
                                 @RequestParam Integer magicbagId,
                                 @RequestParam int quantity) {
        return cartService.addItemToCart(userId, magicbagId, quantity);
    }

    @PutMapping("/{userId}/items/{magicbagId}")
    @Operation(summary = "Update item quantity", description = "Update the quantity of a MagicBag item in the user's cart")
    public CartDto updateItemQuantity(@PathVariable Integer userId,
                                      @PathVariable Integer magicbagId,
                                      @RequestParam int quantity) {
        return cartService.updateItemQuantityInCart(userId, magicbagId, quantity);
    }

    @DeleteMapping("/{userId}/items/{magicbagId}")
    @Operation(summary = "Remove item from cart", description = "Remove a specific MagicBag item from the user's cart")
    public CartDto removeItem(@PathVariable Integer userId,
                              @PathVariable Integer magicbagId) {
        return cartService.removeItemFromCart(userId, magicbagId);
    }

    @GetMapping("/{userId}/items")
    @Operation(summary = "List cart items", description = "Get all items currently in the user's shopping cart")
    public List<CartItemDto> getCartItems(@PathVariable Integer userId) {
        return cartService.getCartItems(userId);
    }

    @DeleteMapping("/{userId}/items")
    @Operation(summary = "Clear cart", description = "Remove all items from the user's shopping cart")
    public CartDto clearCart(@PathVariable Integer userId) {
        return cartService.clearCart(userId);
    }

    @GetMapping("/{userId}/total")
    @Operation(summary = "Get cart total", description = "Calculate the total price of all items in the user's cart")
    public double getTotal(@PathVariable Integer userId) {
        return cartService.getTotal(userId);
    }

    @GetMapping("/items/magicbag/{magicbagId}")
    @Operation(summary = "Get cart items by MagicBag ID", description = "Get cart items that correspond to a specific MagicBag ID")
    public List<CartItemDto> getCartItemsByMagicBagId(@PathVariable Integer magicbagId) {
        return cartService.getCartItemsByMagicBagId(magicbagId);
    }
}
