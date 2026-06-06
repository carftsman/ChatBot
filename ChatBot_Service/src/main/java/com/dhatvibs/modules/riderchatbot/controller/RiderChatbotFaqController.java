package com.dhatvibs.modules.riderchatbot.controller;

import com.dhatvibs.modules.riderchatbot.dto.*;
import com.dhatvibs.modules.riderchatbot.service
        .RiderChatbotFaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/riderchatbot/faq")
@RequiredArgsConstructor
@Tag(name = "Rider Chatbot FAQ",
     description = "Rider Help Center FAQ")
public class RiderChatbotFaqController {

    private final RiderChatbotFaqService faqService;

    @Operation(summary = "1. Get categories")
    @GetMapping("/categories")
    public ResponseEntity<
            List<RiderChatbotCategoryResponse>>
            getCategories() {
        return ResponseEntity.ok(
            faqService.getCategories());
    }

    @Operation(summary = "2. Get questions")
    @GetMapping("/questions/{category}")
    public ResponseEntity<
            List<RiderChatbotFaqQuestionResponse>>
            getQuestions(
            @PathVariable String category) {
        return ResponseEntity.ok(
            faqService.getQuestions(category));
    }

    @Operation(summary = "3. Get answer")
    @GetMapping("/answer/{faqId}")
    public ResponseEntity<
            RiderChatbotFaqAnswerResponse> getAnswer(
            @PathVariable UUID faqId,
            @RequestParam(required = false)
                UUID sessionId) {
        return ResponseEntity.ok(
            faqService.getAnswer(faqId, sessionId));
    }
}