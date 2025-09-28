package nus.iss.se.magicbag.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 用户角色枚举类
 * 定义系统中所有可用的用户角色，包含角色标识、描述
 */
@Getter
@AllArgsConstructor
public enum UserRole {
    /**
     * 超级管理员：拥有系统全部权限
     */
    SUPER_ADMIN("SUPER_ADMIN", "超级管理员"),

    /**
     * 系统管理员：拥有系统管理权限，无用户管理权限
     */
    ADMIN("ADMIN", "系统管理员"),

    /**
     * 普通用户：拥有基础操作权限
     */
    USER("USER", "普通用户"),

    /**
     * 访客：拥有只读权限
     */
    GUEST("GUEST", "访客"),

    /**
     * 商户：拥有商户管理相关权限
     */
    MERCHANT("MERCHANT", "商户"),

    /**
     * 客服：拥有客户服务相关权限
     */
    CUSTOMER_SERVICE("CUSTOMER_SERVICE", "客服");

    /**
     * 角色标识（数据库存储用）
     */
    private final String code;

    /**
     * 角色描述（前端展示用）
     */
    private final String description;


    /**
     * 根据角色编码获取枚举实例
     *
     * @param code 角色编码
     * @return 对应的枚举实例，若不存在则返回空
     */
    public static Optional<UserRole> getByCode(String code) {
        return Arrays.stream(values())
                .filter(role -> role.code.equalsIgnoreCase(code.toUpperCase()))
                .findFirst();
    }
    public static boolean hasRole(String code){
        return getByCode(code).isPresent();
    }
}

