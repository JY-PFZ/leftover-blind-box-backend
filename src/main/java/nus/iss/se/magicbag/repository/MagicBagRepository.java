package nus.iss.se.magicbag.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nus.iss.se.magicbag.entity.CartItem;
import nus.iss.se.magicbag.entity.MagicBag;

public interface MagicBagRepository extends JpaRepository<MagicBag, Integer> {

}
