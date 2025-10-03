package nus.iss.se.magicbag.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一管理redis中的key前缀
 * */
@Getter
@AllArgsConstructor
public enum RedisPrefix {
    AUTH_TOKEN("auth:token:","cache user's token"),
    AUTH_USER("auth:user:","user-token mapping"),
    ACCOUNT_ACTIVATE_TOKEN("auth:activate:token:","activate user account"),

    USER_INFO("user:info:", "cache user's info"),
    MERCHANT_LOCATION("merchant:location", "store merchant's location");

    private final String code;
    private final String description;
}
