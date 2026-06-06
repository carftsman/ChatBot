package com.dhatvibs.modules.config.chat;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class ChatSessionAccess {

    private ChatSessionAccess() {}

    public static void assertOwner(
            String sessionOwnerId,
            String authenticatedId) {
        if (sessionOwnerId == null
                || authenticatedId == null
                || !sessionOwnerId.equals(authenticatedId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied");
        }
    }
}
