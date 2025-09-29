package nus.iss.se.magicbag.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nus.iss.se.magicbag.entity.Cart;
import nus.iss.se.magicbag.entity.CartItem;
import nus.iss.se.magicbag.entity.MagicBag;
import nus.iss.se.magicbag.interfacemethods.CartInterface;
import nus.iss.se.magicbag.repository.CartItemRepository;
import nus.iss.se.magicbag.repository.CartRepository;
import nus.iss.se.magicbag.repository.MagicBagRepository;
import nus.iss.se.magicbag.repository.UserRepository;

@Service

public class CartImpl implements CartInterface {

	    @Autowired
	    private CartRepository cartRepository;

	    @Autowired
	    private CartItemRepository cartItemRepository;

	    @Autowired
	    private UserRepository userRepository;
	    
	    @Autowired
	    private MagicBagRepository magicbagRepository;
	    
	    @Override
	    public Cart createCart(long userId) {
	        Cart cart = new Cart();
	        cart.setUser(userRepository.findById(userId).get());
	        cart.setCreatedAt(LocalDateTime.now());
	        cart.setUpdatedAt(LocalDateTime.now());
	        return cartRepository.save(cart);
	    }
	    
		@Override
		public Cart getActiveCart(long userId) {
	        return cartRepository.findByUserId(userId);
		}
		
	    @Override
	    public Cart addItemToCart(long userId, long magicbagId, int quantity) {
	        Cart cart = getActiveCart(userId);
	        Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndMagicBagId(cart.getCartId(), magicbagId); 
	        MagicBag magicbag = magicbagRepository.findById(magicbagId).get();  
	        
	        if (existingCartItem.isPresent()) {
	            // Update quantity if item already exists in the cart
	            CartItem cartItem = existingCartItem.get();
	            cartItem.setQuantity(cartItem.getQuantity() + quantity);
	            cartItem.setAddedAt(LocalDateTime.now());
	            cartItemRepository.save(cartItem);
	        } else {
	            // Create new cart item
	            CartItem cartItem = new CartItem();
	            cartItem.setCart(cart);
	            cartItem.setMagicBag(magicbag);
	            cartItem.setQuantity(quantity);
	            cartItem.setAddedAt(LocalDateTime.now());
	            cartItemRepository.save(cartItem);
	        }
	        cart.setUpdatedAt(LocalDateTime.now());
	        return cartRepository.save(cart);
	    }
		@Override
		public Cart updateItemQuantityInCart(long userId, long magicbagId, int newQuantity) {
			Cart cart = getActiveCart(userId);
		    Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndMagicBagId(cart.getCartId(), magicbagId);
		    if (newQuantity < 0) {
	            throw new IllegalArgumentException("New quantity cannot be less than zero.");
	        }
	        if (existingCartItem.isPresent()) {
	            CartItem cartItem = existingCartItem.get();
	    
	            // If new quantity is greater than zero, update the existing cart item
	            if (newQuantity > 0) {
	                cartItem.setQuantity(newQuantity);
	                cartItem.setAddedAt(LocalDateTime.now());
	                cartItemRepository.save(cartItem);
	            } else {
	                // If new quantity is zero, remove the item from the cart
	                cartItemRepository.delete(cartItem);
	            }
	        } else {
	            // If newQuantity is zero and the item doesn't exist in the cart, do nothing
	            throw new NoSuchElementException("CartItem not found for the given magicbag ID: " + magicbagId);
	        }
	    
	        // Update the cart's updated time
	        cart.setUpdatedAt(LocalDateTime.now());
	        return cartRepository.save(cart);
		}

		@Override
		public Cart removeItemFromCart(long userId, long magicbagId) {
		    Cart cart = getActiveCart(userId);

		    Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndMagicBagId(
		            cart.getCartId(), magicbagId);

		    existingCartItem.ifPresent(item -> {
		        cartItemRepository.delete(item);
		        cart.getCartItems().remove(item);
		    });

		    cart.setUpdatedAt(LocalDateTime.now());
		    return cartRepository.save(cart);
		}


		@Override
		public List<CartItem> getCartItems(long userId) {
	        Cart cart = getActiveCart(userId);
	        return cart.getCartItems();
		}

		@Override
		public Cart clearCart(long userId) {
		       Cart cart = getActiveCart(userId);
		       cartItemRepository.deleteByCartId(cart.getCartId());
		       cart.setUpdatedAt(LocalDateTime.now());
		       return cartRepository.save(cart);
		}

		@Override
		public double getTotal(long userId) {
	        Cart cart = getActiveCart(userId);
	        double total = cart.getCartItems()
	                           .stream()
	                           .mapToDouble(item -> item.getMagicBag().getPrice() * item.getQuantity())
	                           .sum();
	        return total;
		}

		@Override
		public List<CartItem> getCartItemsByMagicBagId(long id) {
		   	return cartItemRepository.findCartItemsByMagicBagId(id);
		}

}
