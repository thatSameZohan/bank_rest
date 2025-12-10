package com.bank.controller;

import com.bank.dto.*;
import com.bank.entity.UserEntity;
import com.bank.security.JwtService;
import com.bank.service.impl.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest dto) {
        userService.register(dto);
        return ResponseEntity.ok("registered");
    }

    @PostMapping("/login")
    public AuthResponse login (@Valid @RequestBody LoginRequest request,
                                   HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserEntity user = userService.getByUsername(request.username());

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

    @PostMapping("/refresh")
    public AuthResponse refresh(
            @CookieValue("refresh_token") String refreshToken,
            HttpServletResponse response
    ) {
        String username = jwtService.extractUsername(refreshToken);
        UserEntity user = userService.getByUsername(username);

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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Удаляем refresh cookie
        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/v1/api/auth/refresh") // тот же path!
                .sameSite("None")
                .maxAge(0) // удаление
                .build();

        response.addHeader("Set-Cookie", deleteCookie.toString());

        return ResponseEntity.noContent().build();
    }
}
