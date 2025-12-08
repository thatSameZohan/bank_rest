package com.bank.util;

public class CardUtils {
    public static String maskNumber(String plainCardNumber) {
        if (plainCardNumber == null || plainCardNumber.length() < 4) return "****";

        String last4 = plainCardNumber.substring(plainCardNumber.length() - 4);
        return "**** **** **** " + last4;
    }
}
