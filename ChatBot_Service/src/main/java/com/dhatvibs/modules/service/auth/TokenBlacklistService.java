package com.dhatvibs.modules.service.auth;


/**
 * Blacklist interface — stores invalidated tokens after logout.
 * In-memory for Phase 1. Can be replaced with Redis in Phase 2.
 */
public interface TokenBlacklistService {

    void blacklist(String token);

    boolean isBlacklisted(String token);
}
