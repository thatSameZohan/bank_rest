package com.bank.controller;

import com.bank.dto.ErrorDto;
import com.bank.exception.CommonException;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorController {

    @ExceptionHandler(CommonException.class)
    @NullMarked
    public ResponseEntity<ErrorDto> handleCommonException(CommonException exc) {

        ErrorDto error = new ErrorDto(exc.getCode(), exc.getMessage());

        return ResponseEntity.status(error.getCode()).body(error);
    }

}
