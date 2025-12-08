package com.bank.util;

import java.util.UUID;

public class UuidConverter {

    public static UUID ToUUID (String uuid) {

        if (uuid == null || uuid.isEmpty()) {
            throw new IllegalStateException("UUID cannot be null or empty");
        }
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid UUID format");
        }
    }
}
