package nus.iss.se.magicbag.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
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

    /** mybatis plus 分页拦截器*/
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
