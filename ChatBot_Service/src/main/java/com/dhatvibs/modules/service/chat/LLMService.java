/*
 * package com.dhatvibs.modules.service.chat;
 * 
 * import com.fasterxml.jackson.databind.JsonNode; import
 * com.fasterxml.jackson.databind.ObjectMapper; import
 * lombok.extern.slf4j.Slf4j; import
 * org.springframework.beans.factory.annotation.Value; import
 * org.springframework.http.*; import org.springframework.stereotype.Service;
 * import org.springframework.web.client.RestTemplate;
 * 
 * import java.util.*; import java.util.regex.*;
 * 
 * @Slf4j
 * 
 * @Service public class LLMService {
 * 
 * @Value("${llm.api.key}") private String apiKey;
 * 
 * @Value("${llm.api.url}") private String apiUrl;
 * 
 * @Value("${llm.model}") private String model;
 * 
 * @Value("${llm.provider}") private String provider;
 * 
 * private final RestTemplate restTemplate = new RestTemplate(); private final
 * ObjectMapper objectMapper = new ObjectMapper();
 * 
 * public record LLMResult( String intent, boolean needsDB, String directAnswer)
 * {}
 * 
 * // ───────────────────────────────────────────── // KEYWORD MAP — simple
 * contains check // Most reliable — no regex complexity //
 * ───────────────────────────────────────────── private static final
 * Map<String, String> KEYWORD_INTENT_MAP = new LinkedHashMap<>();
 * 
 * static { // ORDER intents KEYWORD_INTENT_MAP.put("where is my order",
 * "track_order"); KEYWORD_INTENT_MAP.put("track my order", "track_order");
 * KEYWORD_INTENT_MAP.put("order status", "track_order");
 * KEYWORD_INTENT_MAP.put("where is order", "track_order");
 * KEYWORD_INTENT_MAP.put("track order", "track_order");
 * KEYWORD_INTENT_MAP.put("order kaha", "track_order");
 * KEYWORD_INTENT_MAP.put("order history", "order_history");
 * KEYWORD_INTENT_MAP.put("past orders", "order_history");
 * KEYWORD_INTENT_MAP.put("my orders", "order_history");
 * KEYWORD_INTENT_MAP.put("previous orders", "order_history");
 * KEYWORD_INTENT_MAP.put("order timeline", "order_timeline");
 * KEYWORD_INTENT_MAP.put("order journey", "order_timeline");
 * KEYWORD_INTENT_MAP.put("cancel order", "cancel_order");
 * KEYWORD_INTENT_MAP.put("cancel my order", "cancel_order");
 * KEYWORD_INTENT_MAP.put("i want to cancel", "cancel_order");
 * KEYWORD_INTENT_MAP.put("order cancel", "cancel_order");
 * KEYWORD_INTENT_MAP.put("delivery late", "delivery_delay");
 * KEYWORD_INTENT_MAP.put("order late", "delivery_delay");
 * KEYWORD_INTENT_MAP.put("taking too long", "delivery_delay");
 * KEYWORD_INTENT_MAP.put("too late", "delivery_delay");
 * KEYWORD_INTENT_MAP.put("not delivered", "order_not_delivered");
 * KEYWORD_INTENT_MAP.put("not received", "order_not_delivered");
 * KEYWORD_INTENT_MAP.put("did not receive", "order_not_delivered");
 * 
 * // PAYMENT intents KEYWORD_INTENT_MAP.put("refund", "refund_status");
 * KEYWORD_INTENT_MAP.put("money back", "refund_status");
 * KEYWORD_INTENT_MAP.put("paisa wapas", "refund_status");
 * KEYWORD_INTENT_MAP.put("where is my refund", "refund_status");
 * KEYWORD_INTENT_MAP.put("refund status", "refund_status");
 * KEYWORD_INTENT_MAP.put("payment failed", "payment_failed");
 * KEYWORD_INTENT_MAP.put("payment not done", "payment_failed");
 * KEYWORD_INTENT_MAP.put("payment fail", "payment_failed");
 * KEYWORD_INTENT_MAP.put("money deducted", "money_deducted");
 * KEYWORD_INTENT_MAP.put("amount deducted", "money_deducted");
 * KEYWORD_INTENT_MAP.put("money cut", "money_deducted");
 * KEYWORD_INTENT_MAP.put("payment pending", "payment_pending");
 * KEYWORD_INTENT_MAP.put("payment stuck", "payment_pending");
 * KEYWORD_INTENT_MAP.put("payment processing", "payment_pending");
 * 
 * // PRODUCT intents KEYWORD_INTENT_MAP.put("item missing", "missing_item");
 * KEYWORD_INTENT_MAP.put("missing item", "missing_item");
 * KEYWORD_INTENT_MAP.put("item not in order", "missing_item");
 * KEYWORD_INTENT_MAP.put("some items missing", "missing_item");
 * KEYWORD_INTENT_MAP.put("wrong item", "wrong_item");
 * KEYWORD_INTENT_MAP.put("wrong product", "wrong_item");
 * KEYWORD_INTENT_MAP.put("received wrong", "wrong_item");
 * KEYWORD_INTENT_MAP.put("damaged", "damaged_item");
 * KEYWORD_INTENT_MAP.put("broken", "damaged_item");
 * KEYWORD_INTENT_MAP.put("spoiled", "damaged_item");
 * 
 * // VENDOR intents KEYWORD_INTENT_MAP.put("new order", "new_order");
 * KEYWORD_INTENT_MAP.put("incoming order", "new_order");
 * KEYWORD_INTENT_MAP.put("payout", "payout_status");
 * KEYWORD_INTENT_MAP.put("settlement", "payout_status");
 * KEYWORD_INTENT_MAP.put("when will i get paid","payout_status");
 * KEYWORD_INTENT_MAP.put("rider not arrived", "rider_not_arrived");
 * KEYWORD_INTENT_MAP.put("rider not coming", "rider_not_arrived");
 * KEYWORD_INTENT_MAP.put("no rider", "rider_not_arrived");
 * 
 * // RIDER intents KEYWORD_INTENT_MAP.put("my earnings", "earnings_today");
 * KEYWORD_INTENT_MAP.put("earnings today", "earnings_today");
 * KEYWORD_INTENT_MAP.put("how much earned", "earnings_today");
 * KEYWORD_INTENT_MAP.put("current order", "current_order");
 * KEYWORD_INTENT_MAP.put("my delivery", "current_order");
 * KEYWORD_INTENT_MAP.put("active order", "current_order");
 * 
 * // ACCOUNT intents KEYWORD_INTENT_MAP.put("cant login", "login_issue");
 * KEYWORD_INTENT_MAP.put("cannot login", "login_issue");
 * KEYWORD_INTENT_MAP.put("login problem", "login_issue");
 * KEYWORD_INTENT_MAP.put("otp not received", "login_issue");
 * KEYWORD_INTENT_MAP.put("app not working", "app_not_working");
 * KEYWORD_INTENT_MAP.put("app crash", "app_not_working");
 * KEYWORD_INTENT_MAP.put("app not opening", "app_not_working");
 * 
 * // SUPPORT intents — these raise ticket
 * KEYWORD_INTENT_MAP.put("talk to support", "talk_to_agent");
 * KEYWORD_INTENT_MAP.put("talk to agent", "talk_to_agent");
 * KEYWORD_INTENT_MAP.put("human support", "talk_to_agent");
 * KEYWORD_INTENT_MAP.put("speak to someone", "talk_to_agent");
 * KEYWORD_INTENT_MAP.put("live agent", "talk_to_agent");
 * KEYWORD_INTENT_MAP.put("emergency", "emergency");
 * KEYWORD_INTENT_MAP.put("accident", "emergency");
 * KEYWORD_INTENT_MAP.put("unsafe", "emergency"); }
 * 
 * // Intents that need DB query private static final Set<String>
 * NEEDS_DB_INTENTS = Set.of( "track_order", "order_status", "order_history",
 * "order_timeline", "cancel_order", "delivery_delay", "order_not_delivered",
 * "payment_failed", "money_deducted", "refund_status", "payment_pending",
 * "payout_status", "payout_not_received", "new_order", "rider_not_arrived",
 * "current_order", "earnings_today", "payout_rider",
 * "payment_not_received_rider" );
 * 
 * // General / conversational keywords private static final List<String>
 * GREETING_WORDS = List.of( "hi", "hello", "hey", "hii", "helo", "hai",
 * "good morning", "good evening", "good afternoon", "namaste", "howdy");
 * 
 * private static final List<String> THANKS_WORDS = List.of( "thank", "thanks",
 * "thankyou", "thank you", "ok", "okay", "got it", "noted", "understood",
 * "cool", "great", "nice", "good", "perfect", "fine", "sure", "alright", "bye",
 * "goodbye");
 * 
 * // ───────────────────────────────────────────── // MAIN METHOD //
 * ───────────────────────────────────────────── public LLMResult
 * processMessage( String userMessage, String userName, String appId) {
 * 
 * String lower = userMessage .toLowerCase().trim();
 * 
 * log.info("Processing message: '{}'", lower);
 * 
 * // STEP 1 — Check greeting first (fastest) if (isGreeting(lower)) { String
 * reply = "Hi " + userName + "! I am your support assistant. " +
 * "How can I help you today?"; log.info("Greeting detected"); return new
 * LLMResult( "general", false, reply); }
 * 
 * // STEP 2 — Check thanks/closing if (isThanks(lower)) { String reply =
 * "You are welcome" + (userName.equals("there") ? "" : " " + userName) +
 * "! Is there anything else " + "I can help you with?";
 * log.info("Thanks detected"); return new LLMResult( "general", false, reply);
 * }
 * 
 * // STEP 3 — Keyword map match (most reliable) String keywordIntent =
 * matchKeyword(lower);
 * 
 * if (keywordIntent != null) { log.info("Keyword match → intent: {}",
 * keywordIntent); boolean needsDB = NEEDS_DB_INTENTS.contains( keywordIntent);
 * return new LLMResult( keywordIntent, needsDB, null); }
 * 
 * // STEP 4 — LLM for complex messages // only if keyword match failed
 * log.info("No keyword match — calling LLM"); try { return callLLMForIntent(
 * lower, userName, appId); } catch (Exception e) { log.error("LLM failed: {}",
 * e.getMessage()); // STEP 5 — Final fallback return new LLMResult(
 * "talk_to_agent", false, null); } }
 * 
 * // ───────────────────────────────────────────── // KEYWORD MATCHING — simple
 * and reliable // ───────────────────────────────────────────── private String
 * matchKeyword(String lower) { for (Map.Entry<String, String> entry :
 * KEYWORD_INTENT_MAP.entrySet()) { if (lower.contains(entry.getKey())) { return
 * entry.getValue(); } } return null; }
 * 
 * private boolean isGreeting(String lower) { // Exact match for single words if
 * (GREETING_WORDS.contains(lower)) return true; // Contains check for (String
 * word : GREETING_WORDS) { if (lower.startsWith(word + " ") ||
 * lower.equals(word)) { return true; } } return false; }
 * 
 * private boolean isThanks(String lower) { for (String word : THANKS_WORDS) {
 * if (lower.equals(word) || lower.startsWith(word + " ") || lower.contains(" "
 * + word)) { return true; } } return false; }
 * 
 * // ───────────────────────────────────────────── // LLM CALL — only for
 * unmatched messages // ───────────────────────────────────────────── private
 * LLMResult callLLMForIntent( String message, String userName, String appId)
 * throws Exception {
 * 
 * String prompt = """ You are a support chatbot for a food delivery app. User:
 * %s, Role: %s
 * 
 * Classify this message into ONE of these intents: track_order, order_history,
 * order_timeline, cancel_order, delivery_delay, order_not_delivered,
 * payment_failed, money_deducted, refund_status, payment_pending, missing_item,
 * wrong_item, damaged_item, login_issue, app_not_working, talk_to_agent,
 * payout_status, new_order, rider_not_arrived, current_order, earnings_today,
 * emergency, general
 * 
 * Message: "%s"
 * 
 * Reply with ONLY this JSON, nothing else:
 * {"intent":"<intent>","needsDB":<true/false>,"directAnswer":<null or
 * "short reply if general">} """.formatted(getRoleName(appId), userName,
 * message);
 * 
 * String raw; if ("claude".equalsIgnoreCase(provider)) { raw =
 * callClaude(prompt); } else { raw = callOpenAI(prompt); }
 * 
 * log.info("LLM raw: {}", raw); return parseJSON(raw, message); }
 * 
 * // ───────────────────────────────────────────── // JSON PARSER //
 * ───────────────────────────────────────────── private LLMResult parseJSON(
 * String raw, String fallbackMessage) {
 * 
 * try { // Extract JSON from response String cleaned = raw
 * .replaceAll("(?s)```json", "") .replaceAll("(?s)```", "") .trim();
 * 
 * // Find JSON object int start = cleaned.indexOf('{'); int end =
 * cleaned.lastIndexOf('}');
 * 
 * if (start == -1 || end == -1) { log.warn("No JSON found in: {}", cleaned);
 * return keywordFallbackOrAgent( fallbackMessage); }
 * 
 * cleaned = cleaned.substring(start, end + 1); log.info("Parsed JSON: {}",
 * cleaned);
 * 
 * JsonNode node = objectMapper.readTree(cleaned);
 * 
 * String intent = node.has("intent") ? node.get("intent").asText() : "general";
 * 
 * boolean needsDB = node.has("needsDB") ? node.get("needsDB").asBoolean() :
 * NEEDS_DB_INTENTS.contains(intent);
 * 
 * String directAnswer = null; if (node.has("directAnswer") &&
 * !node.get("directAnswer") .isNull()) { String ans = node.get("directAnswer")
 * .asText(); if (!ans.equals("null") && !ans.isBlank()) { directAnswer = ans; }
 * }
 * 
 * return new LLMResult( intent, needsDB, directAnswer);
 * 
 * } catch (Exception e) { log.error("JSON parse error: {}", e.getMessage());
 * return keywordFallbackOrAgent( fallbackMessage); } }
 * 
 * // When everything fails — escalate to agent // Better than
 * "I don't understand" private LLMResult keywordFallbackOrAgent( String
 * message) {
 * 
 * // Try keyword one more time String intent = matchKeyword(
 * message.toLowerCase().trim());
 * 
 * if (intent != null) { return new LLMResult( intent,
 * NEEDS_DB_INTENTS.contains(intent), null); }
 * 
 * // Give up — route to support agent // This raises a ticket instead of //
 * saying "I don't understand" return new LLMResult( "talk_to_agent", false,
 * null); }
 * 
 * // ───────────────────────────────────────────── // HELPERS //
 * ───────────────────────────────────────────── private String
 * callClaude(String prompt) throws Exception {
 * 
 * HttpHeaders headers = new HttpHeaders(); headers.setContentType(
 * MediaType.APPLICATION_JSON); headers.set("x-api-key", apiKey);
 * headers.set("anthropic-version", "2023-06-01");
 * 
 * Map<String, Object> body = new HashMap<>(); body.put("model", model);
 * body.put("max_tokens", 200); body.put("messages", List.of( Map.of("role",
 * "user", "content", prompt)));
 * 
 * HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
 * 
 * ResponseEntity<String> response = restTemplate.exchange( apiUrl,
 * HttpMethod.POST, entity, String.class);
 * 
 * JsonNode node = objectMapper .readTree(response.getBody()); return
 * node.at("/content/0/text") .asText().trim(); }
 * 
 * private String callOpenAI(String prompt) throws Exception {
 * 
 * HttpHeaders headers = new HttpHeaders(); headers.setContentType(
 * MediaType.APPLICATION_JSON); headers.setBearerAuth(apiKey);
 * 
 * Map<String, Object> body = new HashMap<>(); body.put("model", model);
 * body.put("max_tokens", 200); body.put("messages", List.of( Map.of("role",
 * "user", "content", prompt)));
 * 
 * HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
 * 
 * ResponseEntity<String> response = restTemplate.exchange( apiUrl,
 * HttpMethod.POST, entity, String.class);
 * 
 * JsonNode node = objectMapper .readTree(response.getBody()); return node.at(
 * "/choices/0/message/content") .asText().trim(); }
 * 
 * private String getRoleName(String appId) { return switch (appId) { case
 * "VENDOR" -> "restaurant vendor"; case "RIDER" -> "delivery rider"; default ->
 * "customer"; }; } }
 */   


package com.dhatvibs.modules.service.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.*;

@Slf4j
@Service
public class LLMService {

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

    public record LLMResult(
        String intent,
        boolean needsDB,
        String directAnswer) {}

    // ── NEW — conversation message holder ────────
    public record ConversationMessage(
        String senderType,   // USER or BOT
        String message) {}

    // ─────────────────────────────────────────────
    // KEYWORD MAP — same as your existing file
    // ─────────────────────────────────────────────
    private static final Map<String, String>
        KEYWORD_INTENT_MAP = new LinkedHashMap<>();

    static {
        // ORDER intents
        KEYWORD_INTENT_MAP.put("where is my order",  "track_order");
        KEYWORD_INTENT_MAP.put("track my order",     "track_order");
        KEYWORD_INTENT_MAP.put("order status",       "track_order");
        KEYWORD_INTENT_MAP.put("where is order",     "track_order");
        KEYWORD_INTENT_MAP.put("track order",        "track_order");
        KEYWORD_INTENT_MAP.put("order kaha",         "track_order");
        KEYWORD_INTENT_MAP.put("order history",      "order_history");
        KEYWORD_INTENT_MAP.put("past orders",        "order_history");
        KEYWORD_INTENT_MAP.put("my orders",          "order_history");
        KEYWORD_INTENT_MAP.put("previous orders",    "order_history");
        KEYWORD_INTENT_MAP.put("order timeline",     "order_timeline");
        KEYWORD_INTENT_MAP.put("order journey",      "order_timeline");
        KEYWORD_INTENT_MAP.put("cancel order",       "cancel_order");
        KEYWORD_INTENT_MAP.put("cancel my order",    "cancel_order");
        KEYWORD_INTENT_MAP.put("i want to cancel",   "cancel_order");
        KEYWORD_INTENT_MAP.put("order cancel",       "cancel_order");
        KEYWORD_INTENT_MAP.put("delivery late",      "delivery_delay");
        KEYWORD_INTENT_MAP.put("order late",         "delivery_delay");
        KEYWORD_INTENT_MAP.put("taking too long",    "delivery_delay");
        KEYWORD_INTENT_MAP.put("too late",           "delivery_delay");
        KEYWORD_INTENT_MAP.put("not delivered",      "order_not_delivered");
        KEYWORD_INTENT_MAP.put("not received",       "order_not_delivered");
        KEYWORD_INTENT_MAP.put("did not receive",    "order_not_delivered");

        // PAYMENT intents
        KEYWORD_INTENT_MAP.put("refund",             "refund_status");
        KEYWORD_INTENT_MAP.put("money back",         "refund_status");
        KEYWORD_INTENT_MAP.put("paisa wapas",        "refund_status");
        KEYWORD_INTENT_MAP.put("where is my refund", "refund_status");
        KEYWORD_INTENT_MAP.put("refund status",      "refund_status");
        KEYWORD_INTENT_MAP.put("payment failed",     "payment_failed");
        KEYWORD_INTENT_MAP.put("payment not done",   "payment_failed");
        KEYWORD_INTENT_MAP.put("payment fail",       "payment_failed");
        KEYWORD_INTENT_MAP.put("money deducted",     "money_deducted");
        KEYWORD_INTENT_MAP.put("amount deducted",    "money_deducted");
        KEYWORD_INTENT_MAP.put("money cut",          "money_deducted");
        KEYWORD_INTENT_MAP.put("payment pending",    "payment_pending");
        KEYWORD_INTENT_MAP.put("payment stuck",      "payment_pending");
        KEYWORD_INTENT_MAP.put("payment processing", "payment_pending");

        // PRODUCT intents
        KEYWORD_INTENT_MAP.put("item missing",       "missing_item");
        KEYWORD_INTENT_MAP.put("missing item",       "missing_item");
        KEYWORD_INTENT_MAP.put("item not in order",  "missing_item");
        KEYWORD_INTENT_MAP.put("some items missing", "missing_item");
        KEYWORD_INTENT_MAP.put("wrong item",         "wrong_item");
        KEYWORD_INTENT_MAP.put("wrong product",      "wrong_item");
        KEYWORD_INTENT_MAP.put("received wrong",     "wrong_item");
        KEYWORD_INTENT_MAP.put("damaged",            "damaged_item");
        KEYWORD_INTENT_MAP.put("broken",             "damaged_item");
        KEYWORD_INTENT_MAP.put("spoiled",            "damaged_item");

        // VENDOR intents
        KEYWORD_INTENT_MAP.put("new order",          "new_order");
        KEYWORD_INTENT_MAP.put("incoming order",     "new_order");
        KEYWORD_INTENT_MAP.put("payout",             "payout_status");
        KEYWORD_INTENT_MAP.put("settlement",         "payout_status");
        KEYWORD_INTENT_MAP.put("when will i get paid","payout_status");
        KEYWORD_INTENT_MAP.put("rider not arrived",  "rider_not_arrived");
        KEYWORD_INTENT_MAP.put("rider not coming",   "rider_not_arrived");
        KEYWORD_INTENT_MAP.put("no rider",           "rider_not_arrived");

        // RIDER intents
        KEYWORD_INTENT_MAP.put("my earnings",        "earnings_today");
        KEYWORD_INTENT_MAP.put("earnings today",     "earnings_today");
        KEYWORD_INTENT_MAP.put("how much earned",    "earnings_today");
        KEYWORD_INTENT_MAP.put("current order",      "current_order");
        KEYWORD_INTENT_MAP.put("my delivery",        "current_order");
        KEYWORD_INTENT_MAP.put("active order",       "current_order");

        // ACCOUNT intents
        KEYWORD_INTENT_MAP.put("cant login",         "login_issue");
        KEYWORD_INTENT_MAP.put("cannot login",       "login_issue");
        KEYWORD_INTENT_MAP.put("login problem",      "login_issue");
        KEYWORD_INTENT_MAP.put("otp not received",   "login_issue");
        KEYWORD_INTENT_MAP.put("app not working",    "app_not_working");
        KEYWORD_INTENT_MAP.put("app crash",          "app_not_working");
        KEYWORD_INTENT_MAP.put("app not opening",    "app_not_working");

        // SUPPORT intents — these raise ticket
        KEYWORD_INTENT_MAP.put("talk to support",    "talk_to_agent");
        KEYWORD_INTENT_MAP.put("talk to agent",      "talk_to_agent");
        KEYWORD_INTENT_MAP.put("human support",      "talk_to_agent");
        KEYWORD_INTENT_MAP.put("speak to someone",   "talk_to_agent");
        KEYWORD_INTENT_MAP.put("live agent",         "talk_to_agent");
        KEYWORD_INTENT_MAP.put("emergency",          "emergency");
        KEYWORD_INTENT_MAP.put("accident",           "emergency");
        KEYWORD_INTENT_MAP.put("unsafe",             "emergency");
    }

    private static final Set<String> NEEDS_DB_INTENTS =
        Set.of(
            "track_order", "order_status",
            "order_history", "order_timeline",
            "cancel_order", "delivery_delay",
            "order_not_delivered", "payment_failed",
            "money_deducted", "refund_status",
            "payment_pending", "payout_status",
            "payout_not_received", "new_order",
            "rider_not_arrived", "current_order",
            "earnings_today", "payout_rider",
            "payment_not_received_rider"
        );

    private static final List<String>
        GREETING_WORDS = List.of(
            "hi", "hello", "hey", "hii", "helo",
            "hai", "good morning", "good evening",
            "good afternoon", "namaste", "howdy");

    private static final List<String>
        THANKS_WORDS = List.of(
            "thank", "thanks", "thankyou",
            "thank you", "ok", "okay", "got it",
            "noted", "understood", "cool", "great",
            "nice", "good", "perfect", "fine",
            "sure", "alright", "bye", "goodbye");

    // ─────────────────────────────────────────────
    // MAIN METHOD — updated with history param
    // ─────────────────────────────────────────────
    public LLMResult processMessage(
            String userMessage,
            String userName,
            String appId,
            List<ConversationMessage> history) { // ← NEW

        String lower = userMessage
            .toLowerCase().trim();

        log.info("Processing: '{}' | history: {}",
                 lower, history.size());

        // Step 1 — greeting
        if (isGreeting(lower)) {
            String reply = "Hi " + userName
                + "! I am your support assistant. "
                + "How can I help you today?";
            return new LLMResult(
                "general", false, reply);
        }

        // Step 2 — thanks
        if (isThanks(lower)) {
            String reply = "You are welcome"
                + (userName.equals("there")
                    ? "" : " " + userName)
                + "! Is there anything else "
                + "I can help you with?";
            return new LLMResult(
                "general", false, reply);
        }

        // Step 3 — keyword match
        String keywordIntent = matchKeyword(lower);
        if (keywordIntent != null) {
            log.info("Keyword match → {}",
                     keywordIntent);
            return new LLMResult(
                keywordIntent,
                NEEDS_DB_INTENTS.contains(
                    keywordIntent),
                null);
        }

        // Step 4 — LLM with conversation memory
        log.info("Calling LLM with {} history messages",
                 history.size());
        try {
            return callLLMWithMemory(
                lower, userName, appId, history);
        } catch (Exception e) {
            log.error("LLM failed: {}", e.getMessage());
            return keywordFallbackOrAgent(lower);
        }
    }

    // ─────────────────────────────────────────────
    // LLM WITH CONVERSATION MEMORY — NEW METHOD
    // ─────────────────────────────────────────────
    private LLMResult callLLMWithMemory(
            String userMessage,
            String userName,
            String appId,
            List<ConversationMessage> history)
            throws Exception {

        // Build history string for prompt
        StringBuilder historyText =
            new StringBuilder();
        if (!history.isEmpty()) {
            historyText.append(
                "Previous conversation:\n");
            history.forEach(msg ->
                historyText
                    .append(msg.senderType())
                    .append(": ")
                    .append(msg.message())
                    .append("\n"));
            historyText.append("\n");
        }

        String prompt = """
You are a support chatbot for a food delivery app.
User name: %s
User role: %s

%sCurrent user message: "%s"

Available intents:
track_order, order_history, order_timeline,
cancel_order, delivery_delay, order_not_delivered,
payment_failed, money_deducted, refund_status,
payment_pending, missing_item, wrong_item,
damaged_item, login_issue, app_not_working,
talk_to_agent, payout_status, new_order,
rider_not_arrived, current_order, earnings_today,
emergency, general

Using the conversation history as context,
classify the current user message.

Reply ONLY with this JSON, nothing else:
{"intent":"<intent>","needsDB":<true/false>,"directAnswer":<null or "short friendly reply">}
""".formatted(
            userName,
            getRoleName(appId),
            historyText.toString(),
            userMessage);

        String raw;
        if ("claude".equalsIgnoreCase(provider)) {
            raw = callClaude(prompt);
        } else {
            raw = callOpenAI(prompt);
        }

        log.info("LLM raw response: {}", raw);
        return parseJSON(raw, userMessage);
    }

    // ─────────────────────────────────────────────
    // KEYWORD MATCHING
    // ─────────────────────────────────────────────
    private String matchKeyword(String lower) {
        for (Map.Entry<String, String> entry
                : KEYWORD_INTENT_MAP.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private boolean isGreeting(String lower) {
        if (GREETING_WORDS.contains(lower))
            return true;
        for (String word : GREETING_WORDS) {
            if (lower.startsWith(word + " ")
                    || lower.equals(word))
                return true;
        }
        return false;
    }

    private boolean isThanks(String lower) {
        for (String word : THANKS_WORDS) {
            if (lower.equals(word)
                    || lower.startsWith(word + " ")
                    || lower.contains(" " + word))
                return true;
        }
        return false;
    }

    // ─────────────────────────────────────────────
    // JSON PARSER — same as your existing
    // ─────────────────────────────────────────────
    private LLMResult parseJSON(
            String raw, String fallbackMessage) {
        try {
            String cleaned = raw
                .replaceAll("(?s)```json", "")
                .replaceAll("(?s)```", "")
                .trim();

            int start = cleaned.indexOf('{');
            int end   = cleaned.lastIndexOf('}');

            if (start == -1 || end == -1) {
                log.warn("No JSON in: {}", cleaned);
                return keywordFallbackOrAgent(
                    fallbackMessage);
            }

            cleaned = cleaned
                .substring(start, end + 1);
            log.info("Parsed JSON: {}", cleaned);

            JsonNode node =
                objectMapper.readTree(cleaned);

            String intent = node.has("intent")
                ? node.get("intent").asText()
                : "general";

            boolean needsDB = node.has("needsDB")
                ? node.get("needsDB").asBoolean()
                : NEEDS_DB_INTENTS.contains(intent);

            String directAnswer = null;
            if (node.has("directAnswer")
                    && !node.get("directAnswer")
                             .isNull()) {
                String ans = node.get("directAnswer")
                    .asText();
                if (!ans.equals("null")
                        && !ans.isBlank()) {
                    directAnswer = ans;
                }
            }

            return new LLMResult(
                intent, needsDB, directAnswer);

        } catch (Exception e) {
            log.error("JSON parse error: {}",
                      e.getMessage());
            return keywordFallbackOrAgent(
                fallbackMessage);
        }
    }

    private LLMResult keywordFallbackOrAgent(
            String message) {
        String intent = matchKeyword(
            message.toLowerCase().trim());
        if (intent != null) {
            return new LLMResult(
                intent,
                NEEDS_DB_INTENTS.contains(intent),
                null);
        }
        return new LLMResult(
            "talk_to_agent", false, null);
    }

    // ─────────────────────────────────────────────
    // API CALLERS — same as your existing
    // ─────────────────────────────────────────────
    private String callClaude(String prompt)
            throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
            MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", 200);
        body.put("messages", List.of(
            Map.of("role", "user",
                   "content", prompt)));

        HttpEntity<Map<String, Object>> entity =
            new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
            restTemplate.exchange(
                apiUrl, HttpMethod.POST,
                entity, String.class);

        JsonNode node = objectMapper
            .readTree(response.getBody());
        return node.at("/content/0/text")
                   .asText().trim();
    }

    private String callOpenAI(String prompt)
            throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
            MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", 200);
        body.put("messages", List.of(
            Map.of("role", "user",
                   "content", prompt)));

        HttpEntity<Map<String, Object>> entity =
            new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
            restTemplate.exchange(
                apiUrl, HttpMethod.POST,
                entity, String.class);

        JsonNode node = objectMapper
            .readTree(response.getBody());
        return node.at(
            "/choices/0/message/content")
            .asText().trim();
    }

    private String getRoleName(String appId) {
        return switch (appId) {
            case "VENDOR" -> "restaurant vendor";
            case "RIDER"  -> "delivery rider";
            default       -> "customer";
        };
    }
}