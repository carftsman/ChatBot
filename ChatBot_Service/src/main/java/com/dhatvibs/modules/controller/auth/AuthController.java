package com.dhatvibs.modules.controller.auth;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dhatvibs.modules.dto.auth.*;
import com.dhatvibs.modules.service.auth.AuthService;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(
    name = "Authentication",
    description = "Login and Logout APIs for User, Vendor, Rider"
)
public class AuthController {

    private final AuthService authService;

    // ─────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────
    @Operation(
        summary = "Login API",
        description = """
            Enter phone number and password.
            Returns a JWT token.
            Token internally contains:
              - sub  = userId  (e.g. USR_001)
              - appId = USER / VENDOR / RIDER
              - role  = USER / VENDOR / RIDER
            Frontend reads appId from decoded token
            to route to correct chatbot UI.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description  = "Login successful — returns JWT token",
            content      = @Content(
                schema   = @Schema(implementation = LoginResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description  = "Invalid phone number or password",
            content      = @Content(
                schema   = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description  = "Account suspended or deleted",
            content      = @Content(
                schema   = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────
    @Operation(
        summary = "Logout API",
        description = """
            Pass the JWT token in request body.
            Token gets blacklisted immediately.
            Any further request with this token
            will be rejected with 401.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description  = "Logout successful",
            content      = @Content(
                schema   = @Schema(implementation = LogoutResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description  = "Already logged out",
            content      = @Content(
                schema   = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description  = "Invalid or expired token",
            content      = @Content(
                schema   = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
            @RequestHeader("Authorization") String authHeader) {

        // Strip "Bearer " prefix
        String token = authHeader.startsWith("Bearer ")
            ? authHeader.substring(7)
            : authHeader;

        LogoutResponse response = authService.logout(token);
        return ResponseEntity.ok(response);
    }
}
