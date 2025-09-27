package nus.iss.se.magicbag.common.exception;

import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.common.Result;
import nus.iss.se.magicbag.common.type.ResultStatus;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handle(Exception e) {
        log.error(ExceptionUtils.getStackTrace(e));
        return ResponseEntity
                .badRequest()
                .body(Result.error(ResultStatus.FAIL,e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handle(MethodArgumentNotValidException e) {
        log.info("Method Argument verification anomaly: {}",ExceptionUtils.getStackTrace(e));
        String errorMessage = Objects.requireNonNull(e.getBindingResult()
                        .getFieldError())
                .getDefaultMessage();
        return ResponseEntity.ok(Result.error(ResultStatus.FAIL, errorMessage));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handle(BusinessException e){
        log.info("Business Handle Exception: {}",ExceptionUtils.getStackTrace(e));
        return ResponseEntity.ok(Result.error(e.getErrInfo(),e.getSupplementMessage()));
    }
}
