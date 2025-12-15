package com.bank.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Фильтр аутентификации JWT, который выполняется один раз на каждый HTTP-запрос.
 *
 * <p>Извлекает JWT-токен из заголовка Authorization, валидирует его,
 * и если токен корректный, устанавливает аутентификацию пользователя
 * в {@link SecurityContextHolder}.</p>
 *
 * <p>Используется Spring Security для защиты REST API и обеспечения stateless аутентификации.</p>
 */
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Сервис для работы с JWT (генерация, проверка и извлечение данных из токена).
     */
    private final JwtService jwtService;

    /**
     * Сервис для загрузки данных пользователя по username.
     */
    private final JpaUserDetailsService userDetailsService;

    /**
     * Основной метод фильтра, который проверяет наличие JWT-токена в запросе.
     *
     * <p>Если токен присутствует и валиден, устанавливает {@link UsernamePasswordAuthenticationToken}
     * в {@link SecurityContextHolder} для дальнейшей авторизации пользователя в Spring Security.</p>
     *
     * @param request  HTTP-запрос
     * @param response HTTP-ответ
     * @param chain    цепочка фильтров
     * @throws IOException      если происходит ошибка ввода/вывода
     * @throws ServletException если происходит ошибка сервлета
     */
    @Override
    @NullMarked
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws IOException, ServletException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(request, response);
    }
}
