package com.bank.controller;

import com.bank.dto.ErrorDto;
import com.bank.dto.TransferDto;
import com.bank.dto.TransferRequestDto;
import com.bank.dto.TransferResponseDto;
import com.bank.entity.UserEntity;
import com.bank.service.TransferService;
import com.bank.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/v1/api/transfers")
@RequiredArgsConstructor
@Tag(
        name = "Transfers",
        description = "API for managing money transfers between user's own cards"
)
public class TransferController {

    private final TransferService transferService;
    private final UserServiceImpl userService;

    @Operation(
            summary = "Perform transfer between user's cards",
            description = "Transfers funds between two cards belonging to the same authenticated user. " +
                    "Authentication is required.",
            requestBody = @RequestBody(
                    description = "Transfer details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TransferRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Transfer completed successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TransferResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request: " +
                                    "- source and destination cards are the same; " +
                                    "- insufficient funds; " +
                                    "- cards don't belong to the user; " +
                                    "- card is not active",
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
    @PostMapping
    public TransferResponseDto transfer(
            @org.springframework.web.bind.annotation.RequestBody TransferRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserEntity user = userService.getByUsername(userDetails.getUsername());
        return transferService.transferBetweenOwnCards(user.getId(), dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    @Operation(
            summary = "Get all transfers (ADMIN only)",
            description = "Retrieves a paginated list of all transfers in the system. " +
                    "Accessible only to administrators.",
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
                            description = "Paginated list of transfers",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - insufficient permissions",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorDto.class)
                            )
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth", scopes = {"ADMIN"})
    )
    @NullMarked
    public Page<TransferDto> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return transferService.getAll(PageRequest.of(page, size));
    }
}
