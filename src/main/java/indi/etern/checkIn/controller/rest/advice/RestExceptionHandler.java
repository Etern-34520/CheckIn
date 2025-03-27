package indi.etern.checkIn.controller.rest.advice;

import lombok.Builder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.UndeclaredThrowableException;

@RestControllerAdvice
public class RestExceptionHandler {
    Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);
    
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorMessage handleNotFoundException(Exception ex) {
        Throwable e = ex;
        if (ex instanceof UndeclaredThrowableException exception) {
            e = exception.getCause();
        }
        logger.error("{}: {}", e.getClass().getName(), e.getMessage());
        if (logger.isDebugEnabled()) {
            e.printStackTrace();
        }
        return ErrorMessage.builder()
                .exception(e.getClass().getName())
                .message(e.getMessage()).build();
    }
    
    @Getter
    @Builder
    public static class ErrorMessage {
        private String exception;
        private String message;
    }
}