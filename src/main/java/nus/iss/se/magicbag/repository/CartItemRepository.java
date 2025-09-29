package nus.iss.se.magicbag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import nus.iss.se.magicbag.entity.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> { 

    @Query("SELECT c FROM CartItem c WHERE c.cart.cartId = :cartId AND c.magicBag.id = :magicBagId AND c.magicBag.isActive = true")
    Optional<CartItem> findByCartIdAndMagicBagId(@Param("cartId") Integer cartId, @Param("magicBagId") Integer magicBagId);

    @Query("SELECT c FROM CartItem c WHERE c.magicBag.id = :id AND c.magicBag.isActive = true")
    List<CartItem> findCartItemsByMagicBagId(@Param("id") Integer id);

    @Query("SELECT c FROM CartItem c WHERE c.cart.cartId = :cartId")
    List<CartItem> findByCartId(@Param("cartId") Integer cartId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.cart.cartId = :cartId")
    void deleteByCartId(@Param("cartId") Integer cartId);
}
