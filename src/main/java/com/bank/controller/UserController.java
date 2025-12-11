package com.bank.controller;

import com.bank.dto.*;
import com.bank.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api")
@Tag(name = "Users", description = "API for user registration, login, logout, and admin operations")
public class UserController {

    private final UserServiceImpl userService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account. Username must be unique.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registered successfully",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "409", description = "User already exists",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest dto) {
        userService.register(dto);
        return ResponseEntity.ok("registered");
    }

    @Operation(
            summary = "Login user and get JWT tokens",
            description = "Authenticates user and returns access token. Refresh token is set in HttpOnly cookie.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    @PostMapping("/auth/login")
    public AuthResponse login (@Valid @RequestBody LoginRequest request,
                                   HttpServletResponse response) {

        return userService.login(request,response);
    }

    @Operation(
            summary = "Refresh JWT tokens",
            description = "Uses refresh token from cookie to issue new access and refresh tokens.",
            parameters = {
                    @Parameter(in = ParameterIn.COOKIE, name = "refresh_token",
                            description = "HTTP-only refresh token", required = true,
                            schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tokens refreshed",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    @PostMapping("/auth/refresh")
    public AuthResponse refresh(
            @CookieValue("refresh_token") String refreshToken,
            HttpServletResponse response) {

        return userService.refresh(refreshToken,response);
    }

    @Operation(
            summary = "Logout user",
            description = "Removes refresh token cookie, invalidating the session.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Logged out successfully")
            }
    )
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        userService.logout(response);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete user by ID",
            description = "Only admins can delete users. Cannot delete another admin.",
            security = @SecurityRequirement(name = "bearerAuth", scopes = {"ADMIN"}),
            parameters = {
                    @Parameter(name = "id", description = "User ID to delete", required = true,
                            in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted"),
                    @ApiResponse(responseCode = "403", description = "Cannot delete admin",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorDto.class))),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public void deleteUser (@PathVariable Long id) {

        userService.deleteById(id);
    }

    @Operation(
            summary = "Block user account",
            description = "Deactivates user's cards (ADMIN only).",
            security = @SecurityRequirement(name = "bearerAuth", scopes = {"ADMIN"}),
            parameters = {
                    @Parameter(name = "id", description = "User ID to block", required = true,
                            in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "User blocked"),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/block")
    public void blockCard (@PathVariable Long id) {

       userService.block(id);
    }

    @Operation(
            summary = "Activate user account",
            description = "Reactivates user's cards (ADMIN only).",
            security = @SecurityRequirement(name = "bearerAuth", scopes = {"ADMIN"}),
            parameters = {
                    @Parameter(name = "id", description = "User ID to activate", required = true,
                            in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "User activated"),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorDto.class)))
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/activate")
    public void activateCard (@PathVariable Long id) {

        userService.activate(id);
    }
}
