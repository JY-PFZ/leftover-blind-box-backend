package nus.iss.se.magicbag.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author mijiupro
 */
@Getter
@AllArgsConstructor
public enum ResultStatus {

    /* 成功状态码 */
    SUCCESS(20000, "SUCCESS"), // 🟢 建议成功码使用 20000

    /* 失败状态码 */
    FAIL(50000, "FAIL"), // 🟢 建议通用失败码使用 50000

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
    USER_ROLE_NOT_FOUND(20006,"User Role Not Found"),
    USER_ACTIVATE_TOKEN_EXPIRE(20007,"Activate Token Expired"),
    USER_ACCOUNT_NOT_ACTIVATE(20008,"User Account Not Activate"),
    // 🟢 新增：商家未找到
    MERCHANT_NOT_FOUND(20009, "Merchant not found."),


    /* 系统错误：40001-49999 */
    FILE_MAX_SIZE_OVERFLOW(40003, "上传尺寸过大"),
    FILE_ACCEPT_NOT_SUPPORT(40004, "上传文件格式不支持"),

    /* 数据错误：50001-59999 */
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
    PERMISSION_SIGNATURE_ERROR(70007, "签名失败"),

    /* 订单错误：80001-89999 */
    ORDER_NOT_FOUND(80001, "订单不存在"),
    ORDER_CANNOT_CANCEL(80002, "订单无法取消"),
    ORDER_ALREADY_CANCELLED(80003, "订单已取消"),
    ORDER_CANNOT_VERIFY(80004, "订单无法核销"),
    ACCESS_DENIED(80005, "无权限访问"); // 这个可以保留，或者用 PERMISSION_UNAUTHORIZED

    // 状态码
    private final int code;
    // 提示信息
    private final String message;
}
