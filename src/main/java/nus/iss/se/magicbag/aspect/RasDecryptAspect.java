package nus.iss.se.magicbag.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.common.BaseProperties;
import nus.iss.se.magicbag.common.annotation.RsaDecrypt;
import nus.iss.se.magicbag.exception.DecryptionFailureException;
import nus.iss.se.magicbag.util.RsaUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class RasDecryptAspect {
    private final RsaUtil rsaUtil;
    private final BaseProperties baseProperties;

    /**
     * 拦截所有 Controller 方法，自动解密标记了 @DecryptField 的字段
     */
    @Around("execution(* nus.iss.se.magicbag.controller..*.*(..))")
    public Object decryptPassword(ProceedingJoinPoint joinPoint) throws Throwable {

        // todo 目前dev环境仅用明文传输
        if (!"dev".equals(baseProperties.getEnv())){
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg != null) {
                    decryptObject(arg);
                }
            }
        }

        return joinPoint.proceed();
    }

    private void decryptObject(Object obj) {
        // 使用 Spring 的反射工具遍历所有字段
        ReflectionUtils.doWithFields(obj.getClass(), field -> {
            try {
                // 检查字段是否有 @RsaDecrypt 注解
                if (!field.isAnnotationPresent(RsaDecrypt.class)) {
                    return;
                }

                // Spring 工具自动处理 setAccessible(true)
                ReflectionUtils.makeAccessible(field);
                Object value = ReflectionUtils.getField(field, obj);

                if (!(value instanceof String encryptedData) || encryptedData.isEmpty()) {
                    log.debug("Skip decryption: The field {} value is empty or not a string", field.getName());
                    return;
                }

                String decrypted = rsaUtil.decrypt(encryptedData);
                ReflectionUtils.setField(field, obj, decrypted);

                log.debug("Decryption successful: {}.{} -> {}", obj.getClass().getSimpleName(), field.getName(), "******");
            } catch (Exception e) {
                log.error("Decryption failure: {}.{}", obj.getClass().getSimpleName(), field.getName(), e);

                if ("password".equals(field.getName())) {
                    throw new DecryptionFailureException("Password decryption failed. Please check the encryption format");
                }
            }
        });
    }
}
