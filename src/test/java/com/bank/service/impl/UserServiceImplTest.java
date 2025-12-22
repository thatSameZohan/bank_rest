package com.bank.service.impl;

import com.bank.dto.LoginRequest;
import com.bank.dto.RegisterRequest;
import com.bank.entity.UserEntity;
import com.bank.enums.Role;
import com.bank.exception.CommonException;
import com.bank.repository.UserRepository;
import com.bank.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    JwtService jwtService;

    @Mock
    HttpServletResponse response;

    @InjectMocks
    UserServiceImpl userService;

    UserEntity user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = UserEntity.builder()
                .id(1L)
                .username("john")
                .password("encodedPass")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();
    }

    // -------------------- REGISTER --------------------

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest("john", "123", false);

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("123")).thenReturn("encoded");

        userService.register(request);

        verify(userRepository).save(argThat(saved ->
                saved.getUsername().equals("john") &&
                        saved.getPassword().equals("encoded") &&
                        saved.getRole().equals(Role.ROLE_USER)
        ));
    }

    @Test
    void register_userAlreadyExists() {
        RegisterRequest request = new RegisterRequest("john", "123", false);

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining("User already exists");
    }

    // -------------------- LOGIN --------------------

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest("john", "123");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any())).thenReturn("access123");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh123");

        var authResponse = userService.login(request, response);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("john", "123")
        );

        verify(response).addCookie(any(Cookie.class));
        assertThat(authResponse.accessToken()).isEqualTo("access123");
    }

    @Test
    void login_userNotFound() {
        LoginRequest request = new LoginRequest("john", "123");

        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request, response))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining("User not found");
    }

    // -------------------- REFRESH --------------------

    @Test
    void refresh_success() {

        when(jwtService.extractUsername("refreshOld")).thenReturn("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any())).thenReturn("newAccess");
        when(jwtService.generateRefreshToken(any())).thenReturn("newRefresh");

        var result = userService.refresh("refreshOld", response);

        verify(response).addCookie(any(Cookie.class));

        assertThat(result.accessToken()).isEqualTo("newAccess");
    }

    // -------------------- LOGOUT --------------------

    @Test
    void logout_success() {
        userService.logout(response);

        verify(response).addHeader(eq("Set-Cookie"), contains("Max-Age=0"));
    }

    // -------------------- DELETE --------------------

    @Test
    void deleteById_success() {
        UserEntity u = user;
        u.setRole(Role.ROLE_USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        userService.deleteById(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteById_admin_forbidden() {
        UserEntity admin = user;
        admin.setRole(Role.ROLE_ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> userService.deleteById(1L))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining("You cannot delete the administrator");
    }

    // -------------------- BLOCK / ACTIVATE --------------------

    @Test
    void block_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.block(1L);

        assertThat(user.isEnabled()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void activate_success() {
        user.setEnabled(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.activate(1L);

        assertThat(user.isEnabled()).isTrue();
        verify(userRepository).save(user);
    }
}
