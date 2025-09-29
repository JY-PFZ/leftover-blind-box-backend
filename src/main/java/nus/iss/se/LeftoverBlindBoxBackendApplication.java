package nus.iss.se;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@MapperScan("nus.iss.se.magicbag.mapper")
public class LeftoverBlindBoxBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeftoverBlindBoxBackendApplication.class, args);
    }

}
