package com.bank.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Базовое исключение приложения.
 *
 * <p>Используется для представления бизнес-ошибок с пользовательским
 * сообщением и HTTP-кодом ошибки.</p>
 *
 * <p>Исключение является unchecked (наследуется от {@link RuntimeException})
 * и может обрабатываться глобальным {@code @ControllerAdvice}.</p>
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class CommonException extends RuntimeException {

    /**
     * HTTP-код ошибки или внутренний код приложения.
     */
    private Integer code;

    /**
     * Описание ошибки, понятное клиенту.
     */
    private String message;
}
