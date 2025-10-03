package nus.iss.se;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
    org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class
})
@MapperScan("nus.iss.se.magicbag.mapper")
public class LeftoverBlindBoxBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeftoverBlindBoxBackendApplication.class, args);
    }
}