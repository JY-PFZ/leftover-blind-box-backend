package nus.iss.se.magicbag.exception;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

public class BusinessException extends RuntimeException {
    @Getter
    private final ResultEnum errInfo;

    @Getter
    private final String supplementMessage;

    public BusinessException(ResultEnum errInfo, String supplementMessage) {
        super(buildMessage(errInfo,supplementMessage));
        this.errInfo = errInfo;
        this.supplementMessage = supplementMessage;
    }

    public BusinessException(ResultEnum errInfo) {
        this(errInfo,null);
    }

    private static String buildMessage(ResultEnum errInfo, String supplement){
        return StringUtils.isBlank(supplement) ? errInfo.getMessage() : errInfo.getMessage() + ": " + supplement;
    }
}
