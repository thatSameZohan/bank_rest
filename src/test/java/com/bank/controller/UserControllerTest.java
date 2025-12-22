package com.bank.controller;

import com.bank.dto.AuthResponse;
import com.bank.dto.LoginRequest;
import com.bank.dto.RegisterRequest;
import com.bank.exception.CommonException;
import com.bank.security.JpaUserDetailsService;
import com.bank.security.JwtAuthenticationFilter;
import com.bank.security.JwtService;
import com.bank.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserServiceImpl userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    // -------------------------
    //      REGISTER
    // -------------------------
    @Test
    @WithMockUser
    void register_success() throws Exception {
        RegisterRequest req = new RegisterRequest("user", "1234", false);

        // Замокать метод register, чтобы ничего не делал
        Mockito.doNothing().when(userService).register(Mockito.any(RegisterRequest.class));

        mockMvc.perform(post("/v1/api/auth/register")
                        .with(csrf()) // CSRF для POST
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("registered"));

        // Проверяем, что метод сервиса вызвался
        Mockito.verify(userService).register(Mockito.any(RegisterRequest.class));
    }

    @Test
    void register_conflict_userExists() throws Exception {

        RegisterRequest req = new RegisterRequest("user", "1234", false);

        Mockito.doThrow(new CommonException(409, "User already exists"))
                .when(userService).register(any());

        mockMvc.perform(post("/v1/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    // -------------------------
    //      LOGIN
    // -------------------------
    @Test
    @WithMockUser
    void login_success() throws Exception {

        LoginRequest req = new LoginRequest("user", "1234");
        AuthResponse auth = new AuthResponse("accessToken");

        Mockito.when(userService.login(any(), any()))
                .thenReturn(auth);

        mockMvc.perform(post("/v1/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"));

        Mockito.verify(userService).login(any(), any());
    }

    // -------------------------
    //      REFRESH TOKEN
    // -------------------------
    @Test
    @WithMockUser
    void refresh_success() throws Exception {

        AuthResponse resp = new AuthResponse("newAccess");
        Mockito.when(userService.refresh(eq("refresh123"), any()))
                .thenReturn(resp);

        mockMvc.perform(post("/v1/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", "refresh123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccess"));

        Mockito.verify(userService).refresh(eq("refresh123"), any());
    }

    // -------------------------
    //      LOGOUT
    // -------------------------
    @Test
    @WithMockUser
    void logout_success() throws Exception {

        mockMvc.perform(post("/v1/api/auth/logout"))
                .andExpect(status().isNoContent());

        Mockito.verify(userService).logout(any());
    }


    // -------------------------
    //      ADMIN — DELETE USER
    // -------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_success() throws Exception {

        mockMvc.perform(delete("/v1/api/users/5"))
                .andExpect(status().isOk());

        Mockito.verify(userService).deleteById(5L);
    }

    // -------------------------
    //      ADMIN — BLOCK USER
    // -------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void blockUser_success() throws Exception {

        mockMvc.perform(post("/v1/api/users/7/block"))
                .andExpect(status().isOk());

        Mockito.verify(userService).block(7L);
    }

    // -------------------------
    //      ADMIN — ACTIVATE USER
    // -------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void activateUser_success() throws Exception {

        mockMvc.perform(post("/v1/api/users/7/activate"))
                .andExpect(status().isOk());

        Mockito.verify(userService).activate(7L);
    }
}
