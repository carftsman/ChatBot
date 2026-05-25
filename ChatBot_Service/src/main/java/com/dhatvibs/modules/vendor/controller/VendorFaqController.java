package com.dhatvibs.modules.vendor.controller;

import com.dhatvibs.modules.vendor.dto.*;
import com.dhatvibs.modules.vendor.service
        .VendorFaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/vendor/faq")
@RequiredArgsConstructor
@Tag(name = "Vendor FAQ",
     description = "Vendor Help Center FAQ")
public class VendorFaqController {

    private final VendorFaqService faqService;

    @Operation(summary = "1. Get categories")
    @GetMapping("/categories")
    public ResponseEntity<
            List<VendorCategoryResponse>>
            getCategories() {
        return ResponseEntity.ok(
            faqService.getCategories());
    }

    @Operation(summary = "2. Get questions")
    @GetMapping("/questions/{category}")
    public ResponseEntity<
            List<VendorFaqQuestionResponse>>
            getQuestions(
            @PathVariable String category) {
        return ResponseEntity.ok(
            faqService.getQuestions(category));
    }

    @Operation(summary = "3. Get answer")
    @GetMapping("/answer/{faqId}")
    public ResponseEntity<VendorFaqAnswerResponse>
            getAnswer(
            @PathVariable UUID faqId,
            @RequestParam(required = false)
                UUID sessionId) {
        return ResponseEntity.ok(
            faqService.getAnswer(faqId, sessionId));
    }
}