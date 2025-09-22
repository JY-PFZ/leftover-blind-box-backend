package nus.iss.se.magicbag.common;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class BaseProperties {
    @Value("${spring.profiles.active}")
    private String env;

    // jwt 密钥
    @Value("${jwt.secret}")
    private String jwtSecret;

    // jwt过期时间，小时计
    @Value("${jwt.expire-Minutes}")
    private Integer jwtExpireMinutes;

    // jwt自动续期时间
    @Value("${jwt.renew-threshold-minutes}")
    private Integer renewThresholdMinutes;
}
