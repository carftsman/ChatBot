package com.dhatvibs.modules.service.chat;


import java.util.List;
import java.util.UUID;

import com.dhatvibs.modules.dto.chat.*;

public interface FaqService {

    /**
     * Step 1 — Get categories for this role.
     * Called when chat opens after user taps order.
     *
     * USER   → ORDER, PAYMENT, DELIVERY,
     *          PRODUCT, ACCOUNT, SUPPORT
     * VENDOR → ORDER, PAYMENT, DELIVERY,
     *          STORE, DISPUTE, SUPPORT
     * RIDER  → EARNINGS, DELIVERY, NAVIGATION,
     *          ACCOUNT, SAFETY, SUPPORT
     */
    List<CategoryResponse> getCategories(String appId);

    /**
     * Step 2 — Get questions under a category.
     * Called when user taps a category button.
     * appId filters questions per role automatically.
     */
    List<FaqQuestionResponse> getQuestions(
            String appId, String category);

    /**
     * Step 3 — Get answer for a tapped question.
     * Called when user taps a specific question.
     *
     * faqId         → from getQuestions response
     * externalUserId → from JWT (X-User-Id header)
     * appId         → from JWT (X-App-Id header)
     * contextOrderId → orderId stored in session
     * sessionId     → from chat/start response
     *
     * If needs_db = TRUE  → fetches live data from DB
     * If needs_db = FALSE → returns static FAQ answer
     * If support intent   → auto raises ticket
     */
    FaqAnswerResponse getAnswer(
            UUID faqId,
            String externalUserId,
            String appId,
            UUID contextOrderId,
            UUID sessionId);
}
