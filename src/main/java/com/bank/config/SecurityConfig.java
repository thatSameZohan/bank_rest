package com.bank.config;

import com.bank.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

/**
 * Конфигурация безопасности приложения.
 *
 * <p>Настраивает Spring Security для работы с JWT-аутентификацией:
 * <ul>
 *     <li>Отключает stateful-сессии (STATELESS)</li>
 *     <li>Отключает CSRF, form-login и HTTP Basic</li>
 *     <li>Добавляет JWT-фильтр в цепочку фильтров</li>
 *     <li>Определяет правила доступа к API</li>
 *     <li>Настраивает CORS для frontend-приложения</li>
 * </ul>
 * </p>
 *
 * <p>Все запросы, кроме аутентификации и документации (Swagger),
 * требуют наличия валидного JWT-токена.</p>
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    /**
     * JWT-фильтр, извлекающий и валидирующий токен из HTTP-заголовка Authorization.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Основная цепочка фильтров Spring Security.
     *
     * <p>Включает:
     * <ul>
     *     <li>Разрешение публичных эндпоинтов (/auth, swagger)</li>
     *     <li>JWT-аутентификацию для остальных запросов</li>
     *     <li>STATLESS-сессию</li>
     *     <li>CORS-настройки</li>
     * </ul>
     * </p>
     *
     * @param http объект {@link HttpSecurity} для конфигурации безопасности
     * @return сконфигурированная {@link SecurityFilterChain}
     * @throws Exception в случае ошибки конфигурации
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v1/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:3000")); // Если фронт на 3000 порту
                    config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
                    config.setAllowedHeaders(List.of("Authorization"));
                    config.setAllowCredentials(true);
                    return config;
                }))
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Бин кодировщика паролей.
     *
     * <p>Используется BCrypt для безопасного хранения паролей пользователей.</p>
     *
     * @return {@link PasswordEncoder} на основе BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Менеджер аутентификации Spring Security.
     *
     * <p>Используется при логине для проверки учетных данных пользователя.</p>
     *
     * @param config конфигурация аутентификации
     * @return {@link AuthenticationManager}
     * @throws Exception в случае ошибки инициализации
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
