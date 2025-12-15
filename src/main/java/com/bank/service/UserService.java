package com.bank.service;

import com.bank.dto.AuthResponse;
import com.bank.dto.LoginRequest;
import com.bank.dto.RegisterRequest;
import com.bank.entity.UserEntity;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Сервис для управления пользователями и аутентификацией.
 *
 * <p>Обеспечивает регистрацию, вход, обновление токенов, выход из системы,
 * управление статусом пользователей (блокировка, активация) и получение информации
 * о пользователях по username или ID.</p>
 *
 * <p>Реализация должна обеспечивать безопасность JWT токенов,
 * корректное управление куками refresh token и проверку ролей при изменении состояния пользователя.</p>
 */
public interface UserService {

    /**
     * Регистрирует нового пользователя.
     *
     * <p>Если пользователь с указанным username уже существует, выбрасывает
     * {@code com.bank.exception.CommonException} с кодом 409.</p>
     *
     * @param request DTO с регистрационными данными пользователя
     * @throws com.bank.exception.CommonException если пользователь уже существует
     */
    void register(RegisterRequest request);

    /**
     * Выполняет аутентификацию пользователя и возвращает access token.
     *
     * <p>Также создает HttpOnly cookie для refresh token.</p>
     *
     * @param request DTO с данными для входа (username, password)
     * @param response HttpServletResponse для установки refresh cookie
     * @return DTO с access token
     */
    AuthResponse login (LoginRequest request, HttpServletResponse response);

    /**
     * Обновляет access token с помощью refresh token.
     *
     * <p>Если refresh token валиден, генерируются новые access и refresh токены,
     * refresh token возвращается в HttpOnly cookie.</p>
     *
     * @param refreshToken refresh token
     * @param response HttpServletResponse для установки новой refresh cookie
     * @return DTO с новым access token
     */
    AuthResponse refresh (String refreshToken, HttpServletResponse response);

    /**
     * Выполняет выход пользователя из системы.
     *
     * <p>Удаляет refresh cookie с клиента.</p>
     *
     * @param response HttpServletResponse для удаления refresh cookie
     */
    void logout(HttpServletResponse response);

    /**
     * Удаляет пользователя по ID.
     *
     * <p>Если пользователь с указанным ID не найден — выбрасывает {@code CommonException} с кодом 404.
     * Нельзя удалять администратора — выбрасывает {@code CommonException} с кодом 403.</p>
     *
     * @param id ID пользователя
     * @throws com.bank.exception.CommonException если пользователь не найден или является администратором
     */
    void deleteById(Long id);

    /**
     * Блокирует пользователя по ID (делает {@code enabled = false}).
     *
     * @param id ID пользователя
     * @throws com.bank.exception.CommonException если пользователь не найден
     */
    void block(Long id);

    /**
     * Активирует пользователя по ID (делает {@code enabled = true}).
     *
     * @param id ID пользователя
     * @throws com.bank.exception.CommonException если пользователь не найден
     */
    void activate(Long id);

    /**
     * Получает пользователя по username.
     *
     * @param username имя пользователя
     * @return сущность пользователя
     * @throws com.bank.exception.CommonException если пользователь не найден
     */
    UserEntity getByUsername(String username);

    /**
     * Получает пользователя по ID.
     *
     * @param id ID пользователя
     * @return сущность пользователя
     * @throws com.bank.exception.CommonException если пользователь не найден
     */
    UserEntity getById(Long id);

}
