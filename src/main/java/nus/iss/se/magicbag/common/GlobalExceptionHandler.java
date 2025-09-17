package nus.iss.se.magicbag.common;

import nus.iss.se.magicbag.dto.Result;
import nus.iss.se.magicbag.exception.UserErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;


@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handle(Exception e) {
        return ResponseEntity
                .badRequest()
                .body(Result.error(e.getMessage()));
    }


    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<?>> handle(AuthenticationException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<?>> handle(MethodArgumentNotValidException e) {
        String errorMessage = Objects.requireNonNull(e.getBindingResult()
                        .getFieldError())
                .getDefaultMessage();
        return ResponseEntity.ok(Result.error(errorMessage));
    }

    @ExceptionHandler(UserErrorException.class)
    public ResponseEntity<Result<?>> handle(UserErrorException e) {
        return ResponseEntity.ok(Result.error(e.getErrInfo()));
    }
}
