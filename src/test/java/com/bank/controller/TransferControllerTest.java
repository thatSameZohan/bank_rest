package com.bank.controller;

import com.bank.dto.TransferRequestDto;
import com.bank.dto.TransferResponseDto;
import com.bank.dto.TransferDto;
import com.bank.entity.UserEntity;
import com.bank.exception.CommonException;
import com.bank.security.JpaUserDetailsService;
import com.bank.security.JwtAuthenticationFilter;
import com.bank.security.JwtService;
import com.bank.service.TransferService;
import com.bank.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransferController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransferService transferService;

    @MockitoBean
    private UserServiceImpl userService;

    private UserEntity user;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1L);
        user.setUsername("user1");
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void testSuccessfulTransfer() throws Exception {
        TransferRequestDto dto = new TransferRequestDto(1L, 2L, new BigDecimal("100"));
        TransferResponseDto response = TransferResponseDto.builder()
                .fromCardMasked("**** **** **** 1111")
                .toCardMasked("**** **** **** 2222")
                .amount(new BigDecimal("100"))
                .fromCardBalanceAfter(new BigDecimal("900"))
                .toCardBalanceAfter(new BigDecimal("1100"))
                .timestamp(LocalDateTime.now())
                .build();

        when(userService.getByUsername("user1")).thenReturn(user);
        when(transferService.transferBetweenOwnCards(user.getId(), dto)).thenReturn(response);

        mockMvc.perform(post("/v1/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCardMasked").value("**** **** **** 1111"))
                .andExpect(jsonPath("$.toCardMasked").value("**** **** **** 2222"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void testTransferInsufficientFunds() throws Exception {
        TransferRequestDto dto = new TransferRequestDto(1L, 2L, new BigDecimal("1000"));

        when(userService.getByUsername("user1")).thenReturn(user);
        when(transferService.transferBetweenOwnCards(user.getId(), dto))
                .thenThrow(new CommonException(400, "Insufficient funds"));

        mockMvc.perform(post("/v1/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient funds"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllTransfersAsAdmin() throws Exception {
        TransferDto transferDto = new TransferDto(
                1L,
                user.getId(),
                null,
                null,
                new BigDecimal("100"),
                LocalDateTime.now()
        );

        Page<TransferDto> page = new PageImpl<>(List.of(transferDto));
        when(transferService.getAll(PageRequest.of(0, 10))).thenReturn(page);

        mockMvc.perform(get("/v1/api/transfers/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }
}
