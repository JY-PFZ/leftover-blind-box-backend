package nus.iss.se.magicbag.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author mijiupro
 */
@Getter
@AllArgsConstructor
public enum ResultEnum {

    /* 成功状态码 */
    SUCCESS(1, "SUCCESS"),

    /* 错误状态码 */
    FAIL(0, "FAIL"),

    /* 参数错误：10001-19999 */
    PARAM_IS_INVALID(10001, "参数无效"),
    PARAM_IS_BLANK(10002, "参数为空"),
    PARAM_TYPE_BIND_ERROR(10003, "Parameter Format Error"),
    PARAM_NOT_COMPLETE(10004, "参数缺失"),

    /* 用户错误：20001-29999*/
    USER_NOT_LOGGED_IN(20001, "用户未登录，请先登录"),
    USER_LOGIN_ERROR(20002, "账号不存在或密码错误"),
    USER_ACCOUNT_FORBIDDEN(20003, "账号已被禁用"),
    USER_NOT_FOUND(20004, "User Not Found"),
    USER_HAS_EXISTED(20005, "User Has Existed"),

    /* 系统错误：40001-49999 */
    FILE_MAX_SIZE_OVERFLOW(40003, "上传尺寸过大"),
    FILE_ACCEPT_NOT_SUPPORT(40004, "上传文件格式不支持"),

    /* 数据错误：50001-599999 */
    RESULT_DATA_NONE(50001, "数据未找到"),
    DATA_IS_WRONG(50002, "数据有误"),
    DATA_ALREADY_EXISTED(50003, "数据已存在"),
    AUTH_CODE_ERROR(50004, "验证码错误"),


    /* 权限错误：70001-79999 */
    PERMISSION_UNAUTHENTICATED(70001, "此操作需要登陆系统！"),

    PERMISSION_UNAUTHORIZED(70002, "权限不足，无权操作！"),

    PERMISSION_EXPIRE(70003, "登录状态过期！"),

    PERMISSION_TOKEN_EXPIRED(70004, "token已过期"),

    PERMISSION_LIMIT(70005, "访问次数受限制"),

    PERMISSION_TOKEN_INVALID(70006, "无效token"),

    PERMISSION_SIGNATURE_ERROR(70007, "签名失败");

    // 状态码
    private final int code;
    // 提示信息
    private final String message;
}
