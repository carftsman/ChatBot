package com.dhatvibs.modules.rider.controller;


import com.dhatvibs.modules.rider.dto.*;
import com.dhatvibs.modules.rider.service.RiderAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/rider/auth")
@RequiredArgsConstructor
@Tag(name = "Auth",
     description = "Rider OTP Login")
public class RiderAuthController {

    private final RiderAuthService authService;

    @Operation(summary = "Step 1 — Send OTP",
        description = "Sends OTP to rider phone")
    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>>
            sendOtp(
            @Valid @RequestBody
            SendOtpRequest request) {
        String msg = authService
            .sendOtp(request.getPhone());
        return ResponseEntity.ok(
            Map.of("message", msg));
    }

    @Operation(summary = "Step 2 — Verify OTP",
        description = "Verifies OTP and returns JWT token")
    @PostMapping("/verify-otp")
    public ResponseEntity<LoginResponse>
            verifyOtp(
            @Valid @RequestBody
            VerifyOtpRequest request) {
        return ResponseEntity.ok(
            authService.verifyOtp(
                request.getPhone(),
                request.getOtp()));
    }
}