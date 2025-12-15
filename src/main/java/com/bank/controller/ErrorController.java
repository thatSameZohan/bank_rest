package com.bank.controller;

import com.bank.dto.ErrorDto;
import com.bank.exception.CommonException;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
/**
 * Глобальный обработчик исключений для REST API.
 *
 * <p>Перехватывает исключения типа {@link CommonException} и формирует
 * корректный {@link ResponseEntity} с телом {@link ErrorDto} и соответствующим HTTP кодом.</p>
 */
@RestControllerAdvice
public class ErrorController {

    /**
     * Обрабатывает исключения {@link CommonException}.
     *
     * @param exc исключение, содержащее код и сообщение ошибки
     * @return {@link ResponseEntity} с {@link ErrorDto} и HTTP статусом, соответствующим коду ошибки
     */
    @ExceptionHandler(CommonException.class)
    @NullMarked
    public ResponseEntity<ErrorDto> handleCommonException(CommonException exc) {

        ErrorDto error = new ErrorDto(exc.getCode(), exc.getMessage());

        return ResponseEntity.status(error.getCode()).body(error);
    }

}
