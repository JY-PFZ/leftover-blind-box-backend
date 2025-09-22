package nus.iss.se.magicbag.dto;

import lombok.Data;
import lombok.Getter;
import nus.iss.se.magicbag.exception.ResultEnum;
import org.apache.commons.lang3.StringUtils;


/**
 * @author mijiupro
 */

@Getter
@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    private Result(ResultEnum resultCode) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    private Result(ResultEnum resultCode, T data) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
    }

    private Result(Integer code, String message){
        this.code = code;
        this.message = message;
    }

    private Result(String message) {
        this.message = message;
    }

    //成功返回封装-无数据
    public static <T> Result<T> success() {
        return new Result<>(ResultEnum.SUCCESS);
    }
    //成功返回封装-带数据
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultEnum.SUCCESS, data);
    }
    //失败返回封装-使用默认提示信息
    public static <T> Result<T> error() {
        return new Result<>(ResultEnum.FAIL);
    }
    //失败返回封装-使用返回结果枚举提示信息
    public static <T> Result<T> error(ResultEnum resultCode) {
        return new Result<>(resultCode);
    }
    //失败返回封装-使用自定义提示信息
    public static <T> Result<T> error(String message) {
        return new Result<>(message);

    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code,message);
    }

    public static <T> Result<T> error(ResultEnum resultCode, String supplementMessage) {
        String msg = StringUtils.isBlank(supplementMessage) ? resultCode.getMessage() : resultCode.getMessage() + ": " + supplementMessage;
        return new Result<>(resultCode.getCode(),msg);
    }
}

