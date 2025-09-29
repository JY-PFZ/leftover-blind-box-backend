package nus.iss.se.magicbag.interfacemethods;

import java.util.List;

import nus.iss.se.magicbag.entity.Cart;
import nus.iss.se.magicbag.entity.CartItem;


public interface CartInterface {
    Cart createCart(long userId);
    Cart getActiveCart(long userId);
    Cart addItemToCart(long userId, long magicbagId, int quantity);
    Cart updateItemQuantityInCart(long userId, long magicbagId, int newQuantity);
    Cart removeItemFromCart(long userId, long magicbagId);
    List<CartItem> getCartItems(long userId);
    Cart clearCart(long userId);
    double getTotal(long userId);
    List<CartItem> getCartItemsByMagicBagId(long id);
}
