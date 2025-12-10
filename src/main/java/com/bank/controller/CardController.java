package com.bank.controller;

import com.bank.dto.*;
import com.bank.entity.UserEntity;
import com.bank.service.impl.CardService;
import com.bank.service.impl.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/cards")
public class CardController {

    private final CardService cardService;
    private final UserService userService;

    @GetMapping
    public Page<CardResponseDto> getMyCards(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UserEntity user = userService.getByUsername(userDetails.getUsername());

        return cardService.getUserCards(user.getId(), PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public CardResponseDto getCard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserEntity user = userService.getByUsername(userDetails.getUsername());
        return cardService.getCardForUser(id, user.getId());
    }

    // --- ADMIN actions ---

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CardResponseDto createCard (@RequestBody CardCreateDto dto) {
        userService.getById(dto.userId());
        return cardService.createCard(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/block")
    public void blockCard (@PathVariable Long id) {
        cardService.blockCard(id);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/activate")
    public void activateCard (@PathVariable Long id) {
        cardService.activateCard(id);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteCard (@PathVariable Long id) {
        cardService.deleteCard(id);
    }
}
