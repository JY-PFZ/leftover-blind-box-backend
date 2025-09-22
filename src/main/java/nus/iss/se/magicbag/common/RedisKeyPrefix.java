package nus.iss.se.magicbag.common;

/**
 * 统一管理redis中的key前缀
 * */
public interface RedisKeyPrefix {
    String AUTH_TOKEN = "auth:token:";
    String AUTH_USER = "auth:user:";

    String USER_INFO = "user:info:";
}
