package com.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO для представления ошибки API.
 *
 * <p>Используется для возврата информации об ошибке клиенту
 * в формате JSON.</p>
 *
 * <p>Возвращается глобальным обработчиком исключений
 * ({@code @ControllerAdvice}).</p>
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class ErrorDto {

    /**
     * HTTP-код ошибки или внутренний код приложения.
     */
    private Integer code;

    /**
     * Описание ошибки, понятное клиенту.
     */
    private String message;
}
