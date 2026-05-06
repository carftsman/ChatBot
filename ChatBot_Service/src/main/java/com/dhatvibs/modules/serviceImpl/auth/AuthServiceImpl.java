package com.dhatvibs.modules.serviceImpl.auth;


import com.dhatvibs.modules.config.auth.JwtUtil;
import com.dhatvibs.modules.dto.auth.LoginRequest;
import com.dhatvibs.modules.dto.auth.LoginResponse;
import com.dhatvibs.modules.dto.auth.LogoutResponse;
import com.dhatvibs.modules.entities.auth.CbUser;
import com.dhatvibs.modules.repository.auth.CbUserRepository;
import com.dhatvibs.modules.service.auth.AuthService;
import com.dhatvibs.modules.service.auth.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CbUserRepository       cbUserRepository;
    private final PasswordEncoder        passwordEncoder;
    private final JwtUtil                jwtUtil;
    private final TokenBlacklistService  tokenBlacklistService;

    @Override
    public LoginResponse login(LoginRequest request) {

        log.info("Login attempt for phone: {}",
                 request.getPhone());

        // Step 1 — find by phone
        CbUser user = cbUserRepository
            .findByPhone(request.getPhone())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid phone number or password"));

        // Step 2 — check status
        if ("SUSPENDED".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Account suspended. Contact support.");
        }

        if ("DELETED".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Account not found. Contact support.");
        }

        // Step 3 — verify password
        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid phone number or password");
        }

        // Step 4 — generate JWT
        // Token internally contains: userId + appId + role
        String token = jwtUtil.generateToken(
            user.getExternalUserId(),  // sub  → e.g. USR_001
            user.getAppId(),           // claim → USER / VENDOR / RIDER
            user.getRole(),            // claim → USER / VENDOR / RIDER
            user.getFullName()         // claim → Ravi Kumar
        );

        log.info("Login successful → userId: {} | appId: {}",
                 user.getExternalUserId(), user.getAppId());

        // Return ONLY the token
        return LoginResponse.builder()
            .token(token)
            .message("Login successful")
            .build();
    }

    @Override
    public LogoutResponse logout(String token) {

        log.info("Logout request received");

        // Validate token first
        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid or expired token");
        }

        // Check already blacklisted
        if (tokenBlacklistService.isBlacklisted(token)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Already logged out");
        }

        // Blacklist the token — it cannot be used again
        tokenBlacklistService.blacklist(token);

        String userId = jwtUtil.extractUserId(token);
        log.info("Logout successful → userId: {}", userId);

        return LogoutResponse.builder()
            .message("Logged out successfully")
            .success(true)
            .build();
    }
}