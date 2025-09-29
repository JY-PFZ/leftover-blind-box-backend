package nus.iss.se.magicbag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import nus.iss.se.magicbag.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{

}
