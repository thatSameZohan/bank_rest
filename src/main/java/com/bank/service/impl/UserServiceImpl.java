package com.bank.service.impl;

import com.bank.dto.AuthResponse;
import com.bank.dto.LoginRequest;
import com.bank.dto.RegisterRequest;
import com.bank.entity.UserEntity;
import com.bank.enums.Role;
import com.bank.exception.CommonException;
import com.bank.repository.UserRepository;
import com.bank.security.JwtService;
import com.bank.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new CommonException (409, "User already exists");
        }

        var user = UserEntity.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(request.isAdmin() ? Role.ROLE_ADMIN : Role.ROLE_USER)
                .enabled(true)
                .build();

        userRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {

        UserEntity user = getByUsername(request.username());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Refresh token → HttpOnly cookie
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true); // true-кука недоступна из JavaScript на стороне клиента, повышает безопасность
        cookie.setSecure(false);       // true-кука будет передаваться только по HTTPS
        cookie.setPath("/"); // кука отправляется только сюда
        cookie.setMaxAge(60 * 60 * 24); // время жизни куки 1 день
        cookie.setDomain("localhost"); //  домен, для которого действительна кука
        cookie.setAttribute("SameSite", "Lax");
        // для SameSite=None обязательно наличие Secure=true (HTTPS), иначе браузер может игнорировать куку.
        // SameSite=Lax (базовая защита) или SameSite=Strict (максимальная защита).
        response.addCookie(cookie);

        // Access token → в body
        return new AuthResponse(accessToken);
    }

    @Override
    public AuthResponse refresh(String refreshToken, HttpServletResponse response) {

        String username = jwtService.extractUsername(refreshToken);
        UserEntity user = getByUsername(username);

        String newAccess = jwtService.generateAccessToken(user);
        String newRefresh = jwtService.generateRefreshToken(user);

        // положить новый refresh cookie
        Cookie cookie = new Cookie("refresh_token", newRefresh);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/v1/api/auth/refresh");
        cookie.setMaxAge(60 * 60 * 24);
        cookie.setAttribute("SameSite", "None");
        response.addCookie(cookie);

        return new AuthResponse(newAccess);
    }

    @Override
    public void logout(HttpServletResponse response) {
        // Удаляем refresh cookie
        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/v1/api/auth/refresh") // тот же path!
                .sameSite("None")
                .maxAge(0) // удаление
                .build();

        response.addHeader("Set-Cookie", deleteCookie.toString());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {

        UserEntity user = getById(id);

        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new CommonException (403, "You cannot delete the administrator");
        }

        userRepository.deleteById(id);
    }

    @Override
    @Transactional
        public void block(Long id) {
        UserEntity user = getById(id);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        UserEntity user = getById(id);
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public UserEntity getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new CommonException (404, "User not found"));
    }

    @Override
    public UserEntity getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CommonException (404, "User not found"));
    }
}
