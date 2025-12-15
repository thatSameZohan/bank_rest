package com.bank.util;

/**
 * Вспомогательный класс для работы с банковскими картами.
 *
 * <p>Содержит метод для маскирования номера карты.</p>
 */
public class CardUtils {

    /**
     * Маскирует номер карты, оставляя видимыми только последние 4 цифры.
     *
     * <p>Пример: {@code "1234567812345678"} → {@code "**** **** **** 5678"}</p>
     *
     * @param plainCardNumber полный номер карты (может быть любой длины ≥ 4)
     * @return маскированный номер карты; если входное значение null или короче 4 символов, возвращает {@code "****"}
     */
    public static String maskNumber(String plainCardNumber) {
        if (plainCardNumber == null || plainCardNumber.length() < 4) return "****";

        String last4 = plainCardNumber.substring(plainCardNumber.length() - 4);
        return "**** **** **** " + last4;
    }
}
