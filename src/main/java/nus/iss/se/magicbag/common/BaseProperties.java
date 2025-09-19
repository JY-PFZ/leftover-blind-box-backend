package nus.iss.se.magicbag.common;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class BaseProperties {
    @Value("${spring.profiles.active}")
    private String env;
}
