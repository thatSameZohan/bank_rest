package com.bank.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Конвертер атрибута номера карты для JPA.
 *
 * <p>Обеспечивает автоматическое шифрование номера карты при сохранении в базу данных
 * и расшифровку при чтении из базы.</p>
 *
 * <p>Использует AES-шифрование с ключом, указанным в {@code application.yaml} через
 * свойство {@code encryption.aes-key}.</p>
 *
 * <p>Пример использования:</p>
 * <pre>
 * &#64;Entity
 * public class CardEntity {
 *     &#64;Convert(converter = CardNumberAttributeConverter.class)
 *     private String cardNumberEncrypted;
 * }
 * </pre>
 */
@Component
@Converter
public class CardNumberAttributeConverter implements AttributeConverter<String, String> {

    private static String AES_KEY;

    /**
     * Конструктор для внедрения ключа AES через Spring.
     *
     * @param aesKey ключ AES для шифрования и дешифрования
     */
    public CardNumberAttributeConverter(@Value("${encryption.aes-key}") String aesKey) {
        AES_KEY = aesKey;
    }

    private static final String ALGO = "AES";

    /**
     * Шифрует номер карты для сохранения в базе данных.
     *
     * @param attribute номер карты (plain text)
     * @return зашифрованный номер карты в формате Base64, или {@code null} если входное значение null
     * @throws RuntimeException при ошибке шифрования
     */
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

    /**
     * Расшифровывает номер карты при чтении из базы данных.
     *
     * @param dbData зашифрованный номер карты в формате Base64
     * @return расшифрованный номер карты (plain text), или {@code null} если входное значение null
     * @throws RuntimeException при ошибке дешифрования
     */
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
