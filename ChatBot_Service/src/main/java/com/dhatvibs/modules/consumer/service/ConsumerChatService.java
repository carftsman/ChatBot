
package com.dhatvibs.modules.consumer.service;

import com.dhatvibs.modules.consumer.dto.*;
import java.util.*;

public interface ConsumerChatService {

    ConsumerChatStartResponse startSession(
            String consumerId,
            String consumerToken,
            String orderId);

    ConsumerSessionStatusResponse resolveOrEscalate(
            UUID sessionId,
            boolean resolved,
            String consumerId);

    ConsumerWebSocketResponse processMessage(
            ConsumerWebSocketRequest request);

    // orderId-based history — industry standard
	/*
	 * ConsumerOrderHistoryResponse getHistoryByOrderId(String orderId);
	 */
    
 // Replace old method
    ConsumerOrderHistoryResponse getHistoryByOrderId(
            String orderId,
            String consumerId,
            int page,
            int size);
}