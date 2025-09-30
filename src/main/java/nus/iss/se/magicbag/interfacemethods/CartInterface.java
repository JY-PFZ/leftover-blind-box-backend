package nus.iss.se.magicbag.interfacemethods;

import java.util.List;

import nus.iss.se.magicbag.dto.CartDto;
import nus.iss.se.magicbag.dto.CartItemDto;
import nus.iss.se.magicbag.entity.Cart;
import nus.iss.se.magicbag.entity.CartItem;


public interface CartInterface {
    CartDto createCart(Integer userId);
    CartDto getActiveCart(Integer userId);
    CartDto addItemToCart(Integer userId, Integer magicbagId, int quantity);
    CartDto updateItemQuantityInCart(Integer userId, Integer magicbagId, int newQuantity);
    CartDto removeItemFromCart(Integer userId, Integer magicbagId);
    List<CartItemDto> getCartItems(Integer userId);
    CartDto clearCart(Integer userId);
    double getTotal(Integer userId);
    List<CartItemDto> getCartItemsByMagicBagId(Integer id);
}

