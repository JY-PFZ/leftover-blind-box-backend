package nus.iss.se.magicbag.interfacemethods;

import java.util.List;

import nus.iss.se.magicbag.entity.Cart;
import nus.iss.se.magicbag.entity.CartItem;


public interface CartInterface {
    Cart createCart(Integer userId);
    Cart getActiveCart(Integer userId);
    Cart addItemToCart(Integer userId, Integer magicbagId, int quantity);
    Cart updateItemQuantityInCart(Integer userId, Integer magicbagId, int newQuantity);
    Cart removeItemFromCart(Integer userId, Integer magicbagId);
    List<CartItem> getCartItems(Integer userId);
    Cart clearCart(Integer userId);
    double getTotal(Integer userId);
    List<CartItem> getCartItemsByMagicBagId(Integer id);
}

