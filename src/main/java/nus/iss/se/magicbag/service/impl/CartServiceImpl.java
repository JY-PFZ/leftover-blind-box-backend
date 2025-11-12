package nus.iss.se.magicbag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.dto.CartDto;
import nus.iss.se.magicbag.dto.CartItemDto;
import nus.iss.se.magicbag.entity.Cart;
import nus.iss.se.magicbag.entity.CartItem;
import nus.iss.se.magicbag.entity.MagicBag;
import nus.iss.se.magicbag.service.ICartService;
import nus.iss.se.magicbag.mapper.CartItemMapper;
import nus.iss.se.magicbag.mapper.CartMapper;
import nus.iss.se.magicbag.mapper.MagicBagMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 购物车服务实现类
 * 核心改动:
 * 全面使用继承自 BaseMapper 的标准方法来操作 CartItem，
 * 替代了所有手写的、可能出错的 SQL 查询。
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final MagicBagMapper magicBagMapper;

    @Override
    public CartDto createCart(Integer userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        // 假设 cartMapper 的方法是正确的
        cartMapper.insertCart(cart);
        return convertToCartDto(cart);
    }

    @Override
    public CartDto getActiveCart(Integer userId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart != null) {
            // 使用 QueryWrapper 安全地查询
            LambdaQueryWrapper<CartItem> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CartItem::getCartId, cart.getCartId());
            List<CartItem> items = cartItemMapper.selectList(queryWrapper);
            cart.setCartItems(items);
        }
        return convertToCartDto(cart);
    }

    @Override
    @Transactional
    public CartDto addItemToCart(Integer userId, Integer magicBagId, int quantity) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            // 创建新购物车
            cart = new Cart();
            cart.setUserId(userId);
            cartMapper.insertCart(cart);
        }

        // 查找购物车中是否已存在该商品
        LambdaQueryWrapper<CartItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CartItem::getCartId, cart.getCartId())
                .eq(CartItem::getMagicBagId, magicBagId);
        CartItem existingItem = cartItemMapper.selectOne(queryWrapper);

        if (existingItem != null) {
            // 更新数量
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setAddedAt(LocalDateTime.now());
            cartItemMapper.updateById(existingItem);
        } else {
            // 插入新商品
            CartItem newItem = new CartItem();
            newItem.setCartId(cart.getCartId());
            newItem.setMagicBagId(magicBagId);
            newItem.setQuantity(quantity);
            cartItemMapper.insert(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());

        return getActiveCart(userId);
    }

    @Override
    @Transactional
    public CartDto updateItemQuantityInCart(Integer userId, Integer magicBagId, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be less than zero.");
        }

        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) throw new NoSuchElementException("Cart not found");

        LambdaQueryWrapper<CartItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CartItem::getCartId, cart.getCartId())
                .eq(CartItem::getMagicBagId, magicBagId);
        CartItem item = cartItemMapper.selectOne(queryWrapper);

        if (item == null) throw new NoSuchElementException("CartItem not found");

        if (newQuantity == 0) {
            // 如果数量为0，则删除
            cartItemMapper.deleteById(item);
        } else {
            // 否则更新数量
            item.setQuantity(newQuantity);
            item.setAddedAt(LocalDateTime.now());
            cartItemMapper.updateById(item);
        }

        return getActiveCart(userId);
    }

    @Override
    @Transactional
    public CartDto removeItemFromCart(Integer userId, Integer magicBagId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) throw new NoSuchElementException("Cart not found for user: " + userId);

        LambdaQueryWrapper<CartItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CartItem::getCartId, cart.getCartId())
                .eq(CartItem::getMagicBagId, magicBagId);
        CartItem item = cartItemMapper.selectOne(queryWrapper);

        if (item != null) {
            // 使用继承自 BaseMapper 的 deleteById 方法，它会正确读取 @TableId
            cartItemMapper.deleteById(item);
        }

        return getActiveCart(userId);
    }

    @Override
    public CartDto clearCart(Integer userId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart != null) {
            LambdaQueryWrapper<CartItem> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CartItem::getCartId, cart.getCartId());
            cartItemMapper.delete(queryWrapper);
        }
        return getActiveCart(userId);
    }

    // --- 以下方法保持不变，因为它们不直接修改 CartItem ---

    @Override
    public List<CartItemDto> getCartItems(Integer userId) {
        // (此方法的原始实现是正确的，无需修改)
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) return new ArrayList<>();

        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cart_id", cart.getCartId());
        List<CartItem> items = cartItemMapper.selectList(queryWrapper);

        return items.stream().map(item -> {
            MagicBag bag = magicBagMapper.selectById(item.getMagicBagId());
            if (bag == null) return null; // Or handle error
            double subtotal = bag.getPrice() * item.getQuantity();
            return new CartItemDto(item.getCartItemId(), item.getMagicBagId(), bag.getTitle(), bag.getPrice(), item.getQuantity(), subtotal);
        }).toList();
    }

    @Override
    public double getTotal(Integer userId) {
        // (此方法的原始实现是正确的，无需修改)
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) return 0.0;

        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cart_id", cart.getCartId());
        List<CartItem> items = cartItemMapper.selectList(queryWrapper);

        return items.stream()
                .mapToDouble(item -> {
                    MagicBag bag = magicBagMapper.selectById(item.getMagicBagId());
                    return (bag != null) ? bag.getPrice() * item.getQuantity() : 0.0;
                })
                .sum();
    }

    @Override
    public List<CartItemDto> getCartItemsByMagicBagId(Integer magicBagId) {
        // (此方法的原始实现是正确的，无需修改)
        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("magic_bag_id", magicBagId);
        List<CartItem> items = cartItemMapper.selectList(queryWrapper);

        MagicBag bag = magicBagMapper.selectById(magicBagId);
        if (bag == null) return new ArrayList<>();

        return items.stream().map(item -> {
            double subtotal = bag.getPrice() * item.getQuantity();
            return new CartItemDto(
                    item.getCartItemId(),
                    item.getMagicBagId(),
                    bag.getTitle(),
                    bag.getPrice(),
                    item.getQuantity(),
                    subtotal
            );
        }).toList();
    }

    private CartDto convertToCartDto(Cart cart) {
        if (cart == null) return null;
        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        List<CartItemDto> items = cartItems.stream().map(item -> {
            MagicBag bag = magicBagMapper.selectById(item.getMagicBagId());
            if (bag == null) return null; // Or handle as needed
            double subtotal = bag.getPrice() * item.getQuantity();
            return new CartItemDto(item.getCartItemId(), item.getMagicBagId(), bag.getTitle(),
                    bag.getPrice(), item.getQuantity(), subtotal);
        }).toList();
        double total = items.stream().mapToDouble(CartItemDto::getSubtotal).sum();
        return new CartDto(cart.getCartId(), cart.getUserId(), items, total);
    }
}

