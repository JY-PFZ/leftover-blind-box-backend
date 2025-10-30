package nus.iss.se.magicbag.common;

import lombok.Data;
import lombok.Getter;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import org.apache.commons.lang3.StringUtils;

@Getter
@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    private Result(ResultStatus resultCode) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    private Result(ResultStatus resultCode, T data) {
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
        return new Result<>(ResultStatus.SUCCESS);
    }
    //成功返回封装-带数据
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultStatus.SUCCESS, data);
    }
    //失败返回封装-使用默认提示信息
    public static <T> Result<T> error() {
        return new Result<>(ResultStatus.FAIL);
    }
    //失败返回封装-使用返回结果枚举提示信息
    public static <T> Result<T> error(ResultStatus resultCode) {
        return new Result<>(resultCode);
    }
    //失败返回封装-使用自定义提示信息
    public static <T> Result<T> error(String message) {
        return new Result<>(message);

    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code,message);
    }

    public static <T> Result<T> error(ResultStatus resultCode, String supplementMessage) {
        String msg = StringUtils.isBlank(supplementMessage) ? resultCode.getMessage() : resultCode.getMessage() + ": " + supplementMessage;
        return new Result<>(resultCode.getCode(),msg);
    }
}

