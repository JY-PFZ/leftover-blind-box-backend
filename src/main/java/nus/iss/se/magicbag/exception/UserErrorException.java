package nus.iss.se.magicbag.exception;

import lombok.Getter;
import nus.iss.se.magicbag.common.ResultEnum;

public class UserErrorException extends RuntimeException{

    @Getter
    private final ResultEnum errInfo;

    public UserErrorException(ResultEnum errEnum) {
        super(errEnum.getMessage());
        this.errInfo = errEnum;
    }
}
