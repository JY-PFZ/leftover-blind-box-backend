package nus.iss.se.magicbag.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class RsaProperties {
    @Value("${rsa.boot}")
    private boolean boot;

    @Value("${rsa.public-key-path}")
    private String publicKeyPath;

    @Value("${rsa.private-key-path}")
    private String privateKeyPath;

    @Setter
    private boolean keyGenerated = false;
}
