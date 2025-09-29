package nus.iss.se.magicbag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nus.iss.se.magicbag.entity.Cart;


public interface CartRepository extends JpaRepository<Cart, Integer> {

    @Query("SELECT c FROM Cart c WHERE c.userId = :userid")
    Cart findByUserId(@Param("userid") Integer userid);
}
