package nus.iss.se.magicbag.service;

import java.util.List;

import nus.iss.se.magicbag.dto.CartDto;
import nus.iss.se.magicbag.dto.CartItemDto;


public interface ICartService {
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

