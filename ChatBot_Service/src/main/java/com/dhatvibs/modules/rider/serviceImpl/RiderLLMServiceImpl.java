package com.dhatvibs.modules.rider.serviceImpl;


import com.dhatvibs.modules.rider.service.RiderLLMService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class RiderLLMServiceImpl
        implements RiderLLMService {

    @Value("${llm.api.key}")
    private String apiKey;
    @Value("${llm.api.url}")
    private String apiUrl;
    @Value("${llm.model}")
    private String model;
    @Value("${llm.provider}")
    private String provider;

    private final RestTemplate restTemplate =
        new RestTemplate();
    private final ObjectMapper objectMapper =
        new ObjectMapper();

    private static final Map<String, String>
        KEYWORD_MAP = new LinkedHashMap<>();

    static {
        KEYWORD_MAP.put("earnings today",    "earnings_today");
        KEYWORD_MAP.put("today earnings",    "earnings_today");
        KEYWORD_MAP.put("how much earned",   "earnings_today");
        KEYWORD_MAP.put("daily earnings",    "earnings_today");
        KEYWORD_MAP.put("weekly earnings",   "earnings_weekly");
        KEYWORD_MAP.put("this week",         "earnings_weekly");
        KEYWORD_MAP.put("week earnings",     "earnings_weekly");
        KEYWORD_MAP.put("total earnings",    "earnings_summary");
        KEYWORD_MAP.put("earnings summary",  "earnings_summary");
        KEYWORD_MAP.put("my earnings",       "earnings_summary");
        KEYWORD_MAP.put("payout",            "earnings_summary");
        KEYWORD_MAP.put("payment",           "earnings_summary");
        KEYWORD_MAP.put("when will i get",   "earnings_summary");
        KEYWORD_MAP.put("cash balance",      "cash_balance");
        KEYWORD_MAP.put("cod cash",          "cash_balance");
        KEYWORD_MAP.put("cash in hand",      "cash_balance");
        KEYWORD_MAP.put("order stats",       "order_stats");
        KEYWORD_MAP.put("my orders",         "order_stats");
        KEYWORD_MAP.put("delivered orders",  "order_stats");
        KEYWORD_MAP.put("order history",     "order_history");
        KEYWORD_MAP.put("past orders",       "order_history");
        KEYWORD_MAP.put("previous orders",   "order_history");
        KEYWORD_MAP.put("my rating",         "ratings");
        KEYWORD_MAP.put("my ratings",        "ratings");
        KEYWORD_MAP.put("performance",       "weekly_performance");
        KEYWORD_MAP.put("weekly rating",     "weekly_performance");
        KEYWORD_MAP.put("my profile",        "rider_profile");
        KEYWORD_MAP.put("profile",           "rider_profile");
        KEYWORD_MAP.put("wallet",            "wallet_balance");
        KEYWORD_MAP.put("wallet balance",    "wallet_balance");
        KEYWORD_MAP.put("document",          "documents");
        KEYWORD_MAP.put("kyc",               "documents");
        KEYWORD_MAP.put("talk to support",   "talk_to_agent");
        KEYWORD_MAP.put("need help",         "talk_to_agent");
        KEYWORD_MAP.put("agent",             "talk_to_agent");
        KEYWORD_MAP.put("emergency",         "emergency");
        KEYWORD_MAP.put("accident",          "emergency");
        KEYWORD_MAP.put("cant login",        "login_issue");
        KEYWORD_MAP.put("login problem",     "login_issue");
        KEYWORD_MAP.put("app not working",   "app_issue");
    }

    private static final Set<String> NEEDS_API =
        Set.of("earnings_today", "earnings_weekly",
               "earnings_summary", "cash_balance",
               "order_stats", "order_history",
               "ratings", "weekly_performance",
               "rider_profile", "wallet_balance",
               "documents");

    private static final List<String> GREETINGS =
        List.of("hi", "hello", "hey", "hii",
                "good morning", "good evening",
                "namaste");

    private static final List<String> THANKS =
        List.of("thank", "thanks", "ok",
                "okay", "got it", "bye",
                "goodbye", "cool", "great");

    @Override
    public LLMResult processMessage(
            String userMessage,
            String riderName,
            List<ConversationMessage> history) {

        String lower = userMessage
            .toLowerCase().trim();

        // Step 1 — greeting
        if (isGreeting(lower)) {
            return new LLMResult("general", false,
                "Hi " + riderName
                + "! I am your support assistant. "
                + "How can I help you today?");
        }

        // Step 2 — thanks
        if (isThanks(lower)) {
            return new LLMResult("general", false,
                "You are welcome! "
                + "Is there anything else I can "
                + "help you with?");
        }

        // Step 3 — keyword match
        String intent = matchKeyword(lower);
        if (intent != null) {
            log.info("Keyword match: {}", intent);
            return new LLMResult(
                intent,
                NEEDS_API.contains(intent),
                null);
        }

        // Step 4 — LLM with history
        try {
            return callLLM(lower, riderName, history);
        } catch (Exception e) {
            log.error("LLM failed: {}",
                      e.getMessage());
            return new LLMResult(
                "talk_to_agent", false, null);
        }
    }

    private LLMResult callLLM(
            String message,
            String riderName,
            List<ConversationMessage> history)
            throws Exception {

        StringBuilder historyText =
            new StringBuilder();
        if (!history.isEmpty()) {
            historyText.append(
                "Previous conversation:\n");
            history.forEach(m ->
                historyText.append(m.senderType())
                    .append(": ")
                    .append(m.message())
                    .append("\n"));
            historyText.append("\n");
        }

        String prompt = """
You are a support chatbot for a food delivery rider app.
Rider name: %s

%sCurrent message: "%s"

Available intents:
earnings_today, earnings_weekly, earnings_summary,
cash_balance, order_stats, order_history,
ratings, weekly_performance, rider_profile,
wallet_balance, documents, login_issue,
app_issue, talk_to_agent, emergency, general

Reply ONLY with JSON, nothing else:
{"intent":"<intent>","needsApi":<true/false>,"directAnswer":<null or "short reply">}
""".formatted(riderName,
              historyText.toString(),
              message);

        String raw = provider.equals("claude")
            ? callClaude(prompt)
            : callOpenAI(prompt);

        log.info("LLM raw: {}", raw);
        return parseJSON(raw, message);
    }

    private LLMResult parseJSON(
            String raw, String fallback) {
        try {
            String cleaned = raw
                .replaceAll("(?s)```json","")
                .replaceAll("(?s)```","")
                .trim();

            int s = cleaned.indexOf('{');
            int e = cleaned.lastIndexOf('}');
            if (s == -1 || e == -1)
                return fallbackResult(fallback);

            cleaned = cleaned.substring(s, e + 1);
            JsonNode n = objectMapper.readTree(cleaned);

            String intent =
                n.path("intent").asText("general");
            boolean needsApi =
                n.path("needsApi").asBoolean(
                    NEEDS_API.contains(intent));
            String answer = null;
            if (!n.path("directAnswer").isNull()
                    && !n.path("directAnswer")
                          .asText().equals("null")) {
                answer = n.path("directAnswer").asText();
            }
            return new LLMResult(
                intent, needsApi, answer);

        } catch (Exception e) {
            return fallbackResult(fallback);
        }
    }

    private LLMResult fallbackResult(
            String message) {
        String intent = matchKeyword(
            message.toLowerCase().trim());
        if (intent != null) {
            return new LLMResult(
                intent,
                NEEDS_API.contains(intent),
                null);
        }
        return new LLMResult(
            "talk_to_agent", false, null);
    }

    private String matchKeyword(String lower) {
        for (Map.Entry<String, String> e
                : KEYWORD_MAP.entrySet()) {
            if (lower.contains(e.getKey()))
                return e.getValue();
        }
        return null;
    }

    private boolean isGreeting(String lower) {
        for (String g : GREETINGS) {
            if (lower.equals(g)
                    || lower.startsWith(g + " "))
                return true;
        }
        return false;
    }

    private boolean isThanks(String lower) {
        for (String t : THANKS) {
            if (lower.equals(t)
                    || lower.contains(" " + t)
                    || lower.startsWith(t + " "))
                return true;
        }
        return false;
    }

    private String callClaude(String prompt)
            throws Exception {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("x-api-key", apiKey);
        h.set("anthropic-version", "2023-06-01");
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", 200);
        body.put("messages",
            List.of(Map.of("role", "user",
                           "content", prompt)));
        ResponseEntity<String> res =
            restTemplate.exchange(apiUrl,
                HttpMethod.POST,
                new HttpEntity<>(body, h),
                String.class);
        return objectMapper.readTree(res.getBody())
            .at("/content/0/text").asText().trim();
    }

    private String callOpenAI(String prompt)
            throws Exception {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setBearerAuth(apiKey);
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", 200);
        body.put("messages",
            List.of(Map.of("role", "user",
                           "content", prompt)));
        ResponseEntity<String> res =
            restTemplate.exchange(apiUrl,
                HttpMethod.POST,
                new HttpEntity<>(body, h),
                String.class);
        return objectMapper.readTree(res.getBody())
            .at("/choices/0/message/content")
            .asText().trim();
    }
}
