package nus.iss.se.magicbag.service.impl;

import nus.iss.se.magicbag.entity.Cart;
import nus.iss.se.magicbag.entity.CartItem;
import nus.iss.se.magicbag.entity.MagicBag;
import nus.iss.se.magicbag.dto.CartDto;
import nus.iss.se.magicbag.dto.CartItemDto;
import nus.iss.se.magicbag.interfacemethods.CartInterface;
import nus.iss.se.magicbag.mapper.CartMapper;
import nus.iss.se.magicbag.mapper.CartItemMapper;
import nus.iss.se.magicbag.mapper.MagicBagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class CartImpl implements CartInterface {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private MagicBagMapper magicBagMapper;

    @Override
    public CartDto createCart(Integer userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cartMapper.insertCart(cart);
        return convertToCartDto(cart);
    }

    @Override
    public CartDto getActiveCart(Integer userId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart != null) {
            cart.setCartItems(cartItemMapper.findByCartId(cart.getCartId()));
        }
        return convertToCartDto(cart);
    }

    @Override
    @Transactional
    public CartDto addItemToCart(Integer userId, Integer magicBagId, int quantity) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cartMapper.insertCart(cart);
        }

        CartItem existing = cartItemMapper.findByCartIdAndMagicBagId(cart.getCartId(), magicBagId);
        MagicBag bag = magicBagMapper.selectById(magicBagId);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setAddedAt(LocalDateTime.now());
            cartItemMapper.updateCartItem(existing);
        } else {
            CartItem item = new CartItem();
            item.setCartId(cart.getCartId());
            item.setMagicBagId(magicBagId);
            item.setQuantity(quantity);
            item.setAddedAt(LocalDateTime.now());
            cartItemMapper.insertCartItem(item);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateCart(cart);

        cart.setCartItems(cartItemMapper.findByCartId(cart.getCartId()));
        return convertToCartDto(cart);
    }

    @Override
    public CartDto updateItemQuantityInCart(Integer userId, Integer magicBagId, int newQuantity) {
        if (newQuantity < 0) throw new IllegalArgumentException("Quantity cannot be less than zero.");

        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) throw new NoSuchElementException("Cart not found");

        CartItem item = cartItemMapper.findByCartIdAndMagicBagId(cart.getCartId(), magicBagId);
        if (item == null) throw new NoSuchElementException("CartItem not found");

        if (newQuantity == 0) {
            cartItemMapper.deleteCartItem(item.getCartItemId());
        } else {
            item.setQuantity(newQuantity);
            item.setAddedAt(LocalDateTime.now());
            cartItemMapper.updateCartItem(item);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateCart(cart);

        cart.setCartItems(cartItemMapper.findByCartId(cart.getCartId()));
        return convertToCartDto(cart);
    }

    @Override
    public CartDto removeItemFromCart(Integer userId, Integer magicBagId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) throw new NoSuchElementException("Cart not found");

        CartItem item = cartItemMapper.findByCartIdAndMagicBagId(cart.getCartId(), magicBagId);
        if (item != null) {
            cartItemMapper.deleteCartItem(item.getCartItemId());
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateCart(cart);

        cart.setCartItems(cartItemMapper.findByCartId(cart.getCartId()));
        return convertToCartDto(cart);
    }

    @Override
    public List<CartItemDto> getCartItems(Integer userId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) return List.of();

        List<CartItem> items = cartItemMapper.findByCartId(cart.getCartId());
        return items.stream().map(item -> {
            MagicBag bag = magicBagMapper.selectById(item.getMagicBagId());
            double subtotal = bag.getPrice() * item.getQuantity();
            return new CartItemDto(item.getCartItemId(), bag.getTitle(), bag.getPrice(), item.getQuantity(), subtotal);
        }).collect(Collectors.toList());
    }

    @Override
    public CartDto clearCart(Integer userId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart != null) {
            cartItemMapper.deleteByCartId(cart.getCartId());
            cart.setUpdatedAt(LocalDateTime.now());
            cartMapper.updateCart(cart);
            cart.setCartItems(List.of());
        }
        return convertToCartDto(cart);
    }

    @Override
    public double getTotal(Integer userId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) return 0.0;

        List<CartItem> items = cartItemMapper.findByCartId(cart.getCartId());
        return items.stream()
                .mapToDouble(item -> magicBagMapper.selectById(item.getMagicBagId()).getPrice() * item.getQuantity())
                .sum();
    }

    @Override
    public List<CartItemDto> getCartItemsByMagicBagId(Integer magicBagId) {
        List<CartItem> items = cartItemMapper.findByMagicBagId(magicBagId);
        
        MagicBag bag = magicBagMapper.selectById(magicBagId);
        
        return items.stream().map(item -> {
            double subtotal = bag.getPrice() * item.getQuantity();
            return new CartItemDto(
                item.getCartItemId(), 
                bag.getTitle(), 
                bag.getPrice(), 
                item.getQuantity(), 
                subtotal
            );
        }).collect(Collectors.toList());
    }

    private CartDto convertToCartDto(Cart cart) {
        if (cart == null) return null;
        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems == null) {
            cartItems = List.of();
        }
        List<CartItemDto> items = cartItems.stream().map(item -> {
            MagicBag bag = magicBagMapper.selectById(item.getMagicBagId());
            double subtotal = bag.getPrice() * item.getQuantity();
            return new CartItemDto(item.getCartItemId(), bag.getTitle(), 
                                   bag.getPrice(), item.getQuantity(), subtotal);
        }).collect(Collectors.toList());
        double total = items.stream().mapToDouble(CartItemDto::getSubtotal).sum();
        return new CartDto(cart.getCartId(), cart.getUserId(), items, total);
    }
}
