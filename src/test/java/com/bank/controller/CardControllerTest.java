package com.bank.controller;

import com.bank.dto.*;
import com.bank.entity.UserEntity;
import com.bank.security.JpaUserDetailsService;
import com.bank.security.JwtAuthenticationFilter;
import com.bank.security.JwtService;
import com.bank.service.CardService;
import com.bank.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private UserServiceImpl userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    void getUserCards_success() throws Exception {
        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("john");

        when(userService.getByUsername("john")).thenReturn(user);

        CardResponseDto dto = new CardResponseDto(1L, 10L, "**** 1111", "John Wick",
                LocalDate.now().plusYears(1), "ACTIVE", new BigDecimal("1000"));
        Page<CardResponseDto> page = new PageImpl<>(List.of(dto));

        when(cardService.getUserCards(eq(10L), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/v1/api/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].maskedNumber").value("**** 1111"));
    }

    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    void getCardById_success() throws Exception {
        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setUsername("john");

        when(userService.getByUsername("john")).thenReturn(user);

        CardResponseDto dto = new CardResponseDto(1L, 10L, "**** 1111", "John Wick",
                LocalDate.now().plusYears(1), "ACTIVE", new BigDecimal("1000"));

        when(cardService.getCardForUser(1L, 10L)).thenReturn(dto);

        mockMvc.perform(get("/v1/api/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maskedNumber").value("**** 1111"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createCard_success() throws Exception {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("admin");

        // Мокаем сервис пользователя
        when(userService.getById(1L)).thenReturn(user);

        // Данные для создания карты
        CardCreateDto dto = new CardCreateDto(
                "1234567812345678",
                "John Wick",
                1L,
                LocalDate.now().plusYears(1),
                new BigDecimal("1000")
        );

        CardResponseDto response = new CardResponseDto(
                1L, 1L, "**** **** **** 5678", "John Wick",
                LocalDate.now().plusYears(1), "ACTIVE", new BigDecimal("1000")
        );

        when(cardService.createCard(dto)).thenReturn(response);

        mockMvc.perform(post("/v1/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maskedNumber").value("**** **** **** 5678"));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void blockCard_success() throws Exception {
        doNothing().when(cardService).blockCard(1L);

        mockMvc.perform(post("/v1/api/cards/1/block"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void activateCard_success() throws Exception {
        doNothing().when(cardService).activateCard(1L);

        mockMvc.perform(post("/v1/api/cards/1/activate"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteCard_success() throws Exception {
        doNothing().when(cardService).deleteCard(1L);

        mockMvc.perform(delete("/v1/api/cards/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllCardsAsAdmin_success() throws Exception {
        CardResponseDto dto = new CardResponseDto(1L, 10L, "**** 1111", "John Wick",
                LocalDate.now().plusYears(1), "ACTIVE", new BigDecimal("1000"));
        Page<CardResponseDto> page = new PageImpl<>(List.of(dto));

        when(cardService.getAllCards(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/v1/api/cards/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

}
