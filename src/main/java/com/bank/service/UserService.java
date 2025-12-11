package com.bank.service;

import com.bank.dto.AuthResponse;
import com.bank.dto.LoginRequest;
import com.bank.dto.RegisterRequest;
import com.bank.entity.UserEntity;
import jakarta.servlet.http.HttpServletResponse;


public interface UserService {

    void register(RegisterRequest request);

    AuthResponse login (LoginRequest request, HttpServletResponse response);

    AuthResponse refresh (String refreshToken, HttpServletResponse response);

    void logout(HttpServletResponse response);

    void deleteById(Long id);

    void block(Long id);

    void activate(Long id);

    UserEntity getByUsername(String username);

    UserEntity getById(Long id);

}
