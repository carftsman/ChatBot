package com.dhatvibs.modules.vendor.service;

import com.dhatvibs.modules.vendor.dto.*;
import java.util.*;

public interface VendorChatService {
    VendorChatStartResponse startSession(
            String vendorId,
            String vendorToken,
            String orderId);

    VendorSessionStatusResponse resolveOrEscalate(
            UUID sessionId,
            boolean resolved,
            String vendorId);

    VendorWebSocketResponse processMessage(
            VendorWebSocketRequest request);

	/*
	 * VendorOrderHistoryResponse getHistoryByOrderId( String orderId);
	 */
    
 // Replace old method
    VendorOrderHistoryResponse getHistoryByOrderId(
            String orderId,
            int page,
            int size);
}