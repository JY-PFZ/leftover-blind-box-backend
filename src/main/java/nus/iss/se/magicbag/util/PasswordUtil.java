package nus.iss.se.magicbag.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordUtil {

    private final PasswordEncoder passwordEncoder;

    /**
     * 加密明文密码
     * @param rawPassword 明文密码
     * @return 加密后的密码（带盐）
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 验证明文密码是否与加密后的密码匹配
     * @param rawPassword 明文密码
     * @param encodedPassword 数据库中存储的加密密码
     * @return true 如果匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 检查密码是否需要重新加密（例如：算法升级）
     * @param encodedPassword 加密后的密码
     * @return 是否过时
     */
    public boolean needsReEncode(String encodedPassword) {
        return passwordEncoder.upgradeEncoding(encodedPassword);
    }
}
