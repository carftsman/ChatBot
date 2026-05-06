package com.dhatvibs.modules.serviceImpl.auth;

import org.springframework.stereotype.Service;

import com.dhatvibs.modules.service.auth.TokenBlacklistService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * In-memory blacklist.
 * When user logs out, token is added here.
 * Gateway checks this before forwarding any request.
 */
@Service
public class TokenBlacklistServiceImpl
        implements TokenBlacklistService {

    // Thread-safe set — survives concurrent logout requests
    private final Set<String> blacklistedTokens =
        Collections.synchronizedSet(new HashSet<>());

    @Override
    public void blacklist(String token) {
        blacklistedTokens.add(token);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
