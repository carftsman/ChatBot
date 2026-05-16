package com.dhatvibs.modules.rider.controller;


import com.dhatvibs.modules.rider.dto.*;
import com.dhatvibs.modules.rider.service.RiderFaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rider/api/faq")
@RequiredArgsConstructor
@Tag(name = "FAQ",
     description = "Rider Help Center FAQ")
public class RiderFaqController {

    private final RiderFaqService faqService;

    @Operation(summary = "1. Get categories",
        description = "Returns category buttons for rider")
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>>
            getCategories() {
        return ResponseEntity.ok(
            faqService.getCategories());
    }

    @Operation(summary = "2. Get questions",
        description = "Returns questions under a category")
    @GetMapping("/questions/{category}")
    public ResponseEntity<List<FaqQuestionResponse>>
            getQuestions(
            @PathVariable String category) {
        return ResponseEntity.ok(
            faqService.getQuestions(category));
    }

    @Operation(summary = "3. Get answer",
        description = "Returns answer — from API or static")
    @GetMapping("/answer/{faqId}")
    public ResponseEntity<FaqAnswerResponse>
            getAnswer(
            @PathVariable UUID faqId,
            @RequestParam(required = false)
                UUID sessionId) {
        return ResponseEntity.ok(
            faqService.getAnswer(faqId, sessionId));
    }
}
