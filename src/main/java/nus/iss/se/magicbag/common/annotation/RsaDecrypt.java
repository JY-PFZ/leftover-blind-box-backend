package nus.iss.se.magicbag.common.annotation;

import java.lang.annotation.*;

/**
 * 标记需要 RSA 解密的字段
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RsaDecrypt {
}
