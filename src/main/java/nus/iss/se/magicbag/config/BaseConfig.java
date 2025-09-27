package nus.iss.se.magicbag.config;

import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.common.properties.RsaProperties;
import nus.iss.se.magicbag.util.RsaUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class BaseConfig {
    @Bean
    public RsaUtil rsaUtil(RsaProperties properties) throws Exception {
        RsaUtil.generateIfNotExists(properties.getPrivateKeyPath(), properties.getPublicKeyPath());
        return new RsaUtil(properties.getPrivateKeyPath(), properties.getPublicKeyPath());
    }
}
