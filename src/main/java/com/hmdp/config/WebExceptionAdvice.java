package com.hmdp.config;

import cn.hutool.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        log.error(e.toString(), e);
        return ResponseEntity.status(HttpStatus.HTTP_INTERNAL_ERROR).body(e.getLocalizedMessage());
    }
}
