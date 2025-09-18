package nus.iss.se;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("nus.iss.se.magicBag.mapper")
public class LeftoverBlindBoxBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeftoverBlindBoxBackendApplication.class, args);
    }

}
