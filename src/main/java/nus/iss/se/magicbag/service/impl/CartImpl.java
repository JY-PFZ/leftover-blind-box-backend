package nus.iss.se.magicbag.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nus.iss.se.magicbag.dto.CartDto;
import nus.iss.se.magicbag.dto.CartItemDto;
import nus.iss.se.magicbag.entity.Cart;
import nus.iss.se.magicbag.entity.CartItem;
import nus.iss.se.magicbag.entity.MagicBag;
import nus.iss.se.magicbag.interfacemethods.CartInterface;
import nus.iss.se.magicbag.mapper.CartItemMapper;
import nus.iss.se.magicbag.mapper.CartMapper;
import nus.iss.se.magicbag.mapper.MagicBagMapper;

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
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.insertCart(cart);
        return convertToCartDTO(cart);
    }

    @Override
    public CartDto getActiveCart(Integer userId) {
        Cart cart = cartMapper.findByUserId(userId);
        return convertToCartDTO(cart);
    }

    @Override
    public CartDto addItemToCart(Integer userId, Integer magicbagId, int quantity) {
        Cart cart = cartMapper.findByUserId(userId);
        CartItem existing = cartItemMapper.findByCartIdAndMagicBagId(cart.getCartId(), magicbagId);
        MagicBag magicBag = magicBagMapper.selectById(magicbagId);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setAddedAt(LocalDateTime.now());
            cartItemMapper.updateCartItem(existing);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setMagicBag(magicBag);
            cartItem.setQuantity(quantity);
            cartItem.setAddedAt(LocalDateTime.now());
            cartItemMapper.insertCartItem(cartItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateCart(cart);
        return convertToCartDTO(cart);
    }

    @Override
    public CartDto updateItemQuantityInCart(Integer userId, Integer magicbagId, int newQuantity) {
        Cart cart = cartMapper.findByUserId(userId);
        CartItem existing = cartItemMapper.findByCartIdAndMagicBagId(cart.getCartId(), magicbagId);

        if (existing == null) {
            throw new NoSuchElementException("CartItem not found for magicBagId: " + magicbagId);
        }

        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be less than zero");
        }

        if (newQuantity == 0) {
            cartItemMapper.deleteCartItem(existing.getCartItemId());
        } else {
            existing.setQuantity(newQuantity);
            existing.setAddedAt(LocalDateTime.now());
            cartItemMapper.updateCartItem(existing);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateCart(cart);
        return convertToCartDTO(cart);
    }

    @Override
    public CartDto removeItemFromCart(Integer userId, Integer magicbagId) {
        Cart cart = cartMapper.findByUserId(userId);
        CartItem existing = cartItemMapper.findByCartIdAndMagicBagId(cart.getCartId(), magicbagId);

        if (existing != null) {
            cartItemMapper.deleteCartItem(existing.getCartItemId());
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateCart(cart);
        return convertToCartDTO(cart);
    }

    @Override
    public List<CartItemDto> getCartItems(Integer userId) {
        Cart cart = cartMapper.findByUserId(userId);
        List<CartItem> items = cartItemMapper.findByCartId(cart.getCartId());
        return items.stream()
                .map(item -> new CartItemDto(
                        item.getCartItemId(),
                        item.getMagicBag().getTitle(),
                        item.getMagicBag().getPrice(),
                        item.getQuantity(),
                        item.getMagicBag().getPrice() * item.getQuantity()
                ))
                .toList();
    }

    @Override
    public CartDto clearCart(Integer userId) {
        Cart cart = cartMapper.findByUserId(userId);
        cartItemMapper.deleteByCartId(cart.getCartId());
        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateCart(cart);
        return convertToCartDTO(cart);
    }

    @Override
    public double getTotal(Integer userId) {
        Cart cart = cartMapper.findByUserId(userId);
        List<CartItem> items = cartItemMapper.findByCartId(cart.getCartId());
        return items.stream().mapToDouble(item -> item.getMagicBag().getPrice() * item.getQuantity()).sum();
    }

    @Override
    public List<CartItemDto> getCartItemsByMagicBagId(Integer magicbagId) {
        List<CartItem> items = cartItemMapper.findCartItemsByMagicBagId(magicbagId);
        return items.stream()
                .map(item -> new CartItemDto(
                        item.getCartItemId(),
                        item.getMagicBag().getTitle(),
                        item.getMagicBag().getPrice(),
                        item.getQuantity(),
                        item.getMagicBag().getPrice() * item.getQuantity()
                ))
                .toList();
    }

    private CartDto convertToCartDTO(Cart cart) {
        List<CartItem> items = cartItemMapper.findByCartId(cart.getCartId());
        List<CartItemDto> itemDTOs = items.stream()
                .map(item -> new CartItemDto(
                        item.getCartItemId(),
                        item.getMagicBag().getTitle(),
                        item.getMagicBag().getPrice(),
                        item.getQuantity(),
                        item.getMagicBag().getPrice() * item.getQuantity()
                ))
                .toList();

        double total = itemDTOs.stream().mapToDouble(CartItemDto::getSubtotal).sum();
        return new CartDto(cart.getCartId(), cart.getUserId(), itemDTOs, total);
    }
}
