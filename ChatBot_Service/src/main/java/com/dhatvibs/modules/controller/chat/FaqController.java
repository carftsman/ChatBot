package com.dhatvibs.modules.controller.chat;


import com.dhatvibs.modules.dto.chat.*;
import com.dhatvibs.modules.service.chat.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
@Tag(
    name = "FAQ",
    description = "Phase 1 — Button based help center")
public class FaqController {

    private final FaqService faqService;

    private String getUserId() {
        return (String) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();
    }

    private String getAppId() {
        return (String) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getCredentials();
    }

    // ── FAQ API 1 — GET CATEGORIES ────────────────
    @Operation(
        summary = "1. Get help center categories",
        description = """
            Called after chat/start.
            Returns category buttons based on JWT appId.
            USER   → Orders, Payments, Delivery,
                     Product, Account, Support
            VENDOR → Order Mgmt, Payments, Rider,
                     Store, Disputes, Support
            RIDER  → Earnings, Orders, Navigation,
                     Account, Safety, Support
            """)
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>>
            getCategories() {
        return ResponseEntity.ok(
            faqService.getCategories(getAppId()));
    }

    // ── FAQ API 2 — GET QUESTIONS ─────────────────
    @Operation(
        summary = "2. Get questions for a category",
        description = """
            Called when user taps a category button.
            Returns question buttons for that category.
            appId is auto-read from JWT — no need to send.
            category values:
              USER   → ORDER, PAYMENT, DELIVERY,
                       PRODUCT, ACCOUNT, SUPPORT
              VENDOR → ORDER, PAYMENT, DELIVERY,
                       STORE, DISPUTE, SUPPORT
              RIDER  → EARNINGS, DELIVERY, NAVIGATION,
                       ACCOUNT, SAFETY, SUPPORT
            """)
    @GetMapping("/questions/{category}")
    public ResponseEntity<List<FaqQuestionResponse>>
            getQuestions(
            @PathVariable String category) {

        return ResponseEntity.ok(
            faqService.getQuestions(
                getAppId(), category));
    }

    // ── FAQ API 3 — GET ANSWER ────────────────────
    @Operation(
        summary = "3. Get answer for a tapped question",
        description = """
            Called when user taps a specific question.

            Path param:
              faqId     → from getQuestions response

            Query params (both optional):
              orderId   → contextOrderId from chat/start
              sessionId → sessionId from chat/start

            Returns answer + two buttons for frontend:
              ✅ Issue Resolved
                 → POST /api/chat/resolve
                   { sessionId, resolved: true }
                 → session closes immediately

              ❌ Issue Not Resolved
                 → POST /api/chat/resolve
                   { sessionId, resolved: false }
                 → chatEnabled becomes TRUE
                 → free WebSocket chat unlocked
            """)
    @GetMapping("/answer/{faqId}")
    public ResponseEntity<FaqAnswerResponse>
            getAnswer(
            @PathVariable UUID faqId,
            @RequestParam(required = false)
                UUID orderId,
            @RequestParam(required = false)
                UUID sessionId) {

        return ResponseEntity.ok(
            faqService.getAnswer(
                faqId,
                getUserId(),
                getAppId(),
                orderId,
                sessionId));
    }
}
