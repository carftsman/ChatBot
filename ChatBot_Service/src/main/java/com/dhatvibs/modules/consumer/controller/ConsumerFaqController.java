package com.dhatvibs.modules.consumer.controller;


import com.dhatvibs.modules.consumer.dto.*;
import com.dhatvibs.modules.consumer.service
        .ConsumerFaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/consumer/faq")
@RequiredArgsConstructor
@Tag(name = "Consumer FAQ",
     description = "Consumer Help Center FAQ")
public class ConsumerFaqController {

    private final ConsumerFaqService faqService;

    @Operation(summary = "1. Get categories")
    @GetMapping("/categories")
    public ResponseEntity<
            List<ConsumerCategoryResponse>>
            getCategories() {
        return ResponseEntity.ok(
            faqService.getCategories());
    }

    @Operation(summary = "2. Get questions")
    @GetMapping("/questions/{category}")
    public ResponseEntity<
            List<ConsumerFaqQuestionResponse>>
            getQuestions(
            @PathVariable String category) {
        return ResponseEntity.ok(
            faqService.getQuestions(category));
    }

    @Operation(summary = "3. Get answer")
    @GetMapping("/answer/{faqId}")
    public ResponseEntity<ConsumerFaqAnswerResponse>
            getAnswer(
            @PathVariable UUID faqId,
            @RequestParam(required = false)
                UUID sessionId) {
        return ResponseEntity.ok(
            faqService.getAnswer(faqId, sessionId));
    }
}