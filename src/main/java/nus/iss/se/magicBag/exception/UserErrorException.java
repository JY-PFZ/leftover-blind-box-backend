package nus.iss.se.magicBag.exception;

import lombok.Getter;
import nus.iss.se.magicBag.common.ResultEnum;

public class UserErrorException extends RuntimeException{

    @Getter
    private final ResultEnum errInfo;

    public UserErrorException(ResultEnum errEnum) {
        super(errEnum.getMessage());
        this.errInfo = errEnum;
    }
}
