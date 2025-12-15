package com.bank.controller;

import com.bank.dto.*;
import com.bank.entity.UserEntity;
import com.bank.service.CardService;
import com.bank.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для управления банковскими картами.
 *
 * <p>Обеспечивает доступ к CRUD операциям карт для пользователей и администраторов:
 * <ul>
 *   <li>Пользователи: просмотр своих карт и баланса</li>
 *   <li>Администраторы: создание, блокировка, активация, удаление карт, просмотр всех карт</li>
 * </ul>
 * </p>
 *
 * <p>API полностью документирован через Swagger / OpenAPI аннотации.</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/cards")
@Tag(
        name = "Cards",
        description = "API for managing user cards and administrative card operations"
)
public class CardController {

    private final CardService cardService;
    private final UserServiceImpl userService;

    @GetMapping
    @NullMarked
    @Operation(
            summary = "Get user's cards",
            description = "Retrieves a paginated list of cards belonging to the authenticated user.",
            parameters = {
                    @Parameter(
                            name = "page",
                            in = ParameterIn.QUERY,
                            description = "Page number (starting from 0)",
                            schema = @Schema(type = "integer", defaultValue = "0", minimum = "0")
                    ),
                    @Parameter(
                            name = "size",
                            in = ParameterIn.QUERY,
                            description = "Page size",
                            schema = @Schema(type = "integer", defaultValue = "10", minimum = "1", maximum = "100")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Paginated list of user's cards",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class)
                            )
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Page<CardResponseDto> getAllCards(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UserEntity user = userService.getByUsername(userDetails.getUsername());

        return cardService.getUserCards(user.getId(), PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get specific card by ID",
            description = "Retrieves details of a specific card. The card must belong to the authenticated user.",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Card ID to retrieve",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(type = "integer", format = "int64")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Card details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CardResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Card does not belong to user",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Card not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorDto.class)
                            )
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
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
    @Operation(
            summary = "Create new card",
            description = "Creates a new card for a specified user. Admin-only operation.",
            requestBody = @RequestBody(
                    description = "Card creation details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CardCreateDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Card created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CardResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request: expiry date in past",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Card already exists",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorDto.class)
                            )
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth", scopes = {"ADMIN"})
    )
    public CardResponseDto createCard (@org.springframework.web.bind.annotation.RequestBody CardCreateDto dto) {
        userService.getById(dto.userId());
        return cardService.createCard(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/block")
    @Operation(
            summary = "Block a card",
            description = "Blocks a card by its ID. Available only for ADMIN users.",
            security = @SecurityRequirement(name = "bearer-token")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Card successfully blocked"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden: user does not have ADMIN role"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Card not found"
            )
    })
    public void blockCard (@PathVariable Long id) {
        cardService.blockCard(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/activate")
    @Operation(
            summary = "Activate a card",
            description = "Activates a card by its ID. Available only for ADMIN users.",
            security = @SecurityRequirement(name = "bearer-token")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "2 Newton",
                    description = "Card successfully activated"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden: user does not have ADMIN role"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Card not found"
            )
    })
    public void activateCard (@PathVariable Long id) {
        cardService.activateCard(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a card",
            description = "Deletes a card by its ID. Available only for ADMIN users.",
            security = @SecurityRequirement(name = "bearer-token")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Card successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden: user does not have ADMIN role"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Card not found"
            )
    })
    public void deleteCard (@PathVariable Long id) {
        cardService.deleteCard(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    @NullMarked
    @Operation(
            summary = "Get all cards (paginated)",
            description = "Retrieves a paginated list of all cards. Available only for ADMIN users.",
            security = @SecurityRequirement(name = "bearer-token")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved card list",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden: user does not have ADMIN role"
            )
    })
    public Page<CardResponseDto> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return cardService.getAllCards(PageRequest.of(page, size));
    }
}
