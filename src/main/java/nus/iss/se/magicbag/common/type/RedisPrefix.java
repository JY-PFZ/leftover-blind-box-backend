package nus.iss.se.magicbag.common.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一管理redis中的key前缀
 * */
@Getter
@AllArgsConstructor
public enum RedisPrefix {
    AUTH_TOKEN("auth:token:"),
    AUTH_USER("auth:user:"),
    USER_INFO("user:info:");

    private final String code;
}
