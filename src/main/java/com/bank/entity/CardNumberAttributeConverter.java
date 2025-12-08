package com.bank.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@Converter
public class CardNumberAttributeConverter implements AttributeConverter<String, String> {

    private static String AES_KEY;

    public CardNumberAttributeConverter(@Value("${encryption.aes-key}") String aesKey) {
        AES_KEY = aesKey;
    }

    private static final String ALGO = "AES";

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(), ALGO);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] enc = cipher.doFinal(attribute.getBytes());
            return Base64.getEncoder().encodeToString(enc);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting card number", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(), ALGO);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] dec = cipher.doFinal(Base64.getDecoder().decode(dbData));
            return new String(dec);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting card number", e);
        }
    }
}
