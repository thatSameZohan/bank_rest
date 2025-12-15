package com.bank.security;


import com.bank.entity.UserEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * Сервис для работы с JWT (JSON Web Token).
 * <p>Отвечает за генерацию, валидацию и извлечение данных из токенов доступа и обновления.</p>
 *
 * <p>Используется в Spring Security для аутентификации пользователей.
 * Токены подписываются с использованием секретного ключа</p>
 */
@Service
public class JwtService {

    /**
     * Секретный ключ для подписи JWT, берется из application.yaml.
     */
    @Value("${security.jwt.secret}")
    private String secret;

    /**
     * Время жизни access-токена в миллисекундах.
     */
    @Value("${security.jwt.expiration.access}")
    private long accessExpiration;

    /**
     * Время жизни refresh-токена в миллисекундах.
     */
    @Value("${security.jwt.expiration.refresh}")
    private long refreshExpiration;

    /**
     * Генерирует ключ для подписи токена на основе секретной строки.
     *
     * @return {@link Key} для подписи JWT
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Генерирует JWT access-токен для указанного пользователя.
     *
     * @param user пользователь {@link UserEntity} для которого создается токен
     * @return JWT access-токен в виде строки
     */
    public String generateAccessToken(UserEntity user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Генерирует JWT refresh-токен для указанного пользователя.
     *
     * @param user пользователь {@link UserEntity} для которого создается токен
     * @return JWT refresh-токен в виде строки
     */
    public String generateRefreshToken(UserEntity user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Извлекает имя пользователя (username) из JWT-токена.
     *
     * @param token JWT токен
     * @return имя пользователя (subject)
     */
    public String extractUsername(String token) {
        return parseToken(token).getBody().getSubject();
    }

    /**
     * Проверяет, валиден ли токен для указанного пользователя.
     * <p>Токен считается валидным, если имя пользователя совпадает
     * и токен не истёк.</p>
     *
     * @param token JWT токен
     * @param userDetails объект {@link UserDetails} для проверки
     * @return true, если токен валиден, иначе false
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isExpired(token);
    }

    /**
     * Проверяет, истёк ли срок действия токена.
     *
     * @param token JWT токен
     * @return true, если токен просрочен, иначе false
     */
    private boolean isExpired(String token) {
        return parseToken(token).getBody().getExpiration().before(new Date());
    }

    /**
     * Парсит JWT токен и возвращает объект {@link Jws<Claims>}.
     *
     * @param token JWT токен
     * @return разобранный токен с содержимым claims
     */
    private Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
    }
}
