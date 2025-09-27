package nus.iss.se.magicbag.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "common.rsa")
public class RsaProperties {
    private boolean boot;

    private String publicKeyPath;

    private String privateKeyPath;
}
