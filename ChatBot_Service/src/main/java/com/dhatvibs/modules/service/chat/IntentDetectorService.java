package com.dhatvibs.modules.service.chat;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.dhatvibs.modules.entities.chat.CbFaq;
import com.dhatvibs.modules.repository.chat.CbFaqRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntentDetectorService {

    private final CbFaqRepository faqRepository;

    public Optional<CbFaq> detect(String message, String appId) {

        String lower = message.toLowerCase().trim();

        // Fetch FAQs for this appId + ALL
        List<CbFaq> faqs = faqRepository
            .findByAppIdInAndIsActiveTrue(
                List.of(appId, "ALL"));

        CbFaq bestMatch  = null;
        int   bestScore  = 0;

        for (CbFaq faq : faqs) {
            int score = 0;
            for (String keyword : faq.getKeywords()) {
                if (lower.contains(
                        keyword.toLowerCase())) {
                    // longer keyword = more specific = higher score
                    score += keyword.length();
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestMatch = faq;
            }
        }

        if (bestMatch != null && bestScore > 0) {
            log.info("Intent detected: {} | score: {}",
                     bestMatch.getIntent(), bestScore);
            return Optional.of(bestMatch);
        }

        log.info("No intent detected for: {}", message);
        // Return fallback FAQ
        return faqs.stream()
            .filter(f -> "fallback"
                .equals(f.getIntent()))
            .findFirst();
    }
}