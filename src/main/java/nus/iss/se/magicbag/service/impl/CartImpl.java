package nus.iss.se.magicbag.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nus.iss.se.magicbag.dto.CartDto;
import nus.iss.se.magicbag.dto.CartItemDto;
import nus.iss.se.magicbag.entity.Cart;
import nus.iss.se.magicbag.entity.CartItem;
import nus.iss.se.magicbag.entity.MagicBag;
import nus.iss.se.magicbag.interfacemethods.CartInterface;
import nus.iss.se.magicbag.repository.CartItemRepository;
import nus.iss.se.magicbag.repository.CartRepository;
import nus.iss.se.magicbag.repository.MagicBagRepository;

@Service
public class CartImpl implements CartInterface {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private MagicBagRepository magicbagRepository;

    @Override
    public CartDto createCart(Integer userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return convertToCartDTO(cartRepository.save(cart));
    }

    @Override
    public CartDto getActiveCart(Integer userId) {
        Cart cart = cartRepository.findByUserId(userId);
        return convertToCartDTO(cart);
    }

    @Override
    public CartDto addItemToCart(Integer userId, Integer magicbagId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId);
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndMagicBagId(cart.getCartId(), magicbagId);
        MagicBag magicbag = magicbagRepository.findById(magicbagId).orElseThrow();

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setAddedAt(LocalDateTime.now());
            cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setMagicBag(magicbag);
            cartItem.setQuantity(quantity);
            cartItem.setAddedAt(LocalDateTime.now());
            cartItemRepository.save(cartItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        return convertToCartDTO(cartRepository.save(cart));
    }

    @Override
    public CartDto updateItemQuantityInCart(Integer userId, Integer magicbagId, int newQuantity) {
        Cart cart = cartRepository.findByUserId(userId);
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndMagicBagId(cart.getCartId(), magicbagId);

        if (newQuantity < 0) {
            throw new IllegalArgumentException("New quantity cannot be less than zero.");
        }

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            if (newQuantity > 0) {
                cartItem.setQuantity(newQuantity);
                cartItem.setAddedAt(LocalDateTime.now());
                cartItemRepository.save(cartItem);
            } else {
                cartItemRepository.delete(cartItem);
            }
        } else {
            throw new NoSuchElementException("CartItem not found for the given magicbag ID: " + magicbagId);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        return convertToCartDTO(cartRepository.save(cart));
    }

    @Override
    public CartDto removeItemFromCart(Integer userId, Integer magicbagId) {
        Cart cart = cartRepository.findByUserId(userId);
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndMagicBagId(cart.getCartId(), magicbagId);

        existingCartItem.ifPresent(item -> {
            cartItemRepository.delete(item);
            cart.getCartItems().remove(item);
        });

        cart.setUpdatedAt(LocalDateTime.now());
        return convertToCartDTO(cartRepository.save(cart));
    }

    @Override
    public List<CartItemDto> getCartItems(Integer userId) {
        Cart cart = cartRepository.findByUserId(userId);
        return cart.getCartItems().stream()
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
        Cart cart = cartRepository.findByUserId(userId);
        cartItemRepository.deleteByCartId(cart.getCartId());
        cart.setUpdatedAt(LocalDateTime.now());
        return convertToCartDTO(cartRepository.save(cart));
    }

    @Override
    public double getTotal(Integer userId) {
        Cart cart = cartRepository.findByUserId(userId);
        return cart.getCartItems()
                .stream()
                .mapToDouble(item -> item.getMagicBag().getPrice() * item.getQuantity())
                .sum();
    }

    @Override
    public List<CartItemDto> getCartItemsByMagicBagId(Integer magicbagid) {
        return cartItemRepository.findCartItemsByMagicBagId(magicbagid)
                .stream()
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
        List<CartItemDto> itemDTOs = cart.getCartItems().stream()
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
