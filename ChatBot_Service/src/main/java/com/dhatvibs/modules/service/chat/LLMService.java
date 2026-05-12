/*
 * package com.dhatvibs.modules.service.chat;
 * 
 * 
 * import com.fasterxml.jackson.databind.JsonNode; import
 * com.fasterxml.jackson.databind.ObjectMapper; import
 * lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j; import
 * org.springframework.beans.factory.annotation.Value; import
 * org.springframework.http.*; import org.springframework.stereotype.Service;
 * import org.springframework.web.client.RestTemplate;
 * 
 * import java.util.*;
 * 
 * @Slf4j
 * 
 * @Service
 * 
 * @RequiredArgsConstructor public class LLMService {
 * 
 * @Value("${llm.api.key}") private String apiKey;
 * 
 * @Value("${llm.api.url}") private String apiUrl;
 * 
 * @Value("${llm.model}") private String model;
 * 
 * @Value("${llm.provider}") private String provider;
 * 
 * private final RestTemplate restTemplate = new RestTemplate();
 * 
 * private final ObjectMapper objectMapper = new ObjectMapper();
 * 
 * // List of all known intents in your system private static final List<String>
 * KNOWN_INTENTS = List.of( "track_order", "order_status", "order_history",
 * "order_timeline", "cancel_order", "delivery_delay", "order_not_delivered",
 * "payment_failed", "money_deducted", "refund_status", "payment_pending",
 * "missing_item", "wrong_item", "damaged_item", "login_issue",
 * "app_not_working", "talk_to_agent", "payout_status", "payout_not_received",
 * "new_order", "rider_not_arrived", "current_order", "earnings_today",
 * "emergency", "general" // for hi, hello, thanks etc. );
 * 
 *//**
	 * Step 1 — Classify user message into an intent. Returns intent string like
	 * "track_order" or "general"
	 */
/*
 * public String classifyIntent( String userMessage, String appId) {
 * 
 * String prompt = buildClassificationPrompt( userMessage, appId);
 * 
 * String llmResponse = callLLM(prompt);
 * 
 * // Parse intent from LLM response return extractIntent(llmResponse); }
 * 
 *//**
	 * Step 2 — Get direct conversational answer from LLM. Used for general messages
	 * like "hi", "thanks" etc.
	 */
/*
 * public String getDirectAnswer( String userMessage, String userName, String
 * appId) {
 * 
 * String prompt = buildConversationalPrompt( userMessage, userName, appId);
 * 
 * return callLLM(prompt); }
 * 
 *//**
	 * Combined method — classify AND answer if general. Returns LLMResult with
	 * intent + optional direct answer.
	 *//*
		 * public LLMResult processMessage( String userMessage, String userName, String
		 * appId) {
		 * 
		 * // Build combined prompt — classify + answer in one call String prompt =
		 * buildCombinedPrompt( userMessage, userName, appId);
		 * 
		 * String response = callLLM(prompt);
		 * 
		 * return parseCombinedResponse(response); }
		 * 
		 * // ───────────────────────────────────────────── // PROMPT BUILDERS //
		 * ─────────────────────────────────────────────
		 * 
		 * private String buildClassificationPrompt( String message, String appId) {
		 * 
		 * return """ You are an intent classifier for a food delivery support chatbot.
		 * 
		 * The user is a %s (appId=%s).
		 * 
		 * Known intents: %s
		 * 
		 * User message: "%s"
		 * 
		 * Respond with ONLY the intent name from the list. If message is
		 * greeting/thanks/general conversation, respond with: general
		 * 
		 * Intent: """.formatted( getRoleName(appId), appId, String.join(", ",
		 * KNOWN_INTENTS), message); }
		 * 
		 * private String buildConversationalPrompt( String message, String userName,
		 * String appId) {
		 * 
		 * return """ You are a helpful support assistant for a food delivery app (like
		 * Swiggy/Zomato). The user's name is %s. They are a %s.
		 * 
		 * Keep responses short, friendly, and professional. Do not make up any order or
		 * payment information. If asked about orders or payments, say you will check
		 * and ask them to use the FAQ options.
		 * 
		 * User: %s Assistant: """.formatted( userName, getRoleName(appId), message); }
		 * 
		 * private String buildCombinedPrompt( String message, String userName, String
		 * appId) {
		 * 
		 * return """ You are a support chatbot for a food delivery app. User name: %s
		 * User role: %s
		 * 
		 * Known intents: %s
		 * 
		 * User message: "%s"
		 * 
		 * Respond in this exact JSON format: { "intent":
		 * "intent_name_from_list_or_general", "needsDB": true/false, "directAnswer":
		 * "your answer if general, else null" }
		 * 
		 * Rules: - If message matches a known intent, set needsDB=true and
		 * directAnswer=null - If message is greeting/thanks/general chat, set
		 * intent=general, needsDB=false, directAnswer="your friendly response here" -
		 * Never make up order or payment data - Keep directAnswer short and friendly
		 * 
		 * JSON response: """.formatted( userName, getRoleName(appId), String.join(", ",
		 * KNOWN_INTENTS), message); }
		 * 
		 * // ───────────────────────────────────────────── // LLM API CALLER //
		 * ─────────────────────────────────────────────
		 * 
		 * private String callLLM(String prompt) { try { if ("claude".equals(provider))
		 * { return callClaude(prompt); } else { return callOpenAI(prompt); } } catch
		 * (Exception e) { log.error("LLM call failed: {}", e.getMessage()); return
		 * "general"; // fallback } }
		 * 
		 * private String callClaude(String prompt) throws Exception { HttpHeaders
		 * headers = new HttpHeaders();
		 * headers.setContentType(MediaType.APPLICATION_JSON); headers.set("x-api-key",
		 * apiKey); headers.set("anthropic-version", "2023-06-01");
		 * 
		 * Map<String, Object> body = new HashMap<>(); body.put("model", model);
		 * body.put("max_tokens", 200); body.put("messages", List.of( Map.of("role",
		 * "user", "content", prompt) ));
		 * 
		 * HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
		 * 
		 * ResponseEntity<String> response = restTemplate.exchange( apiUrl,
		 * HttpMethod.POST, entity, String.class);
		 * 
		 * JsonNode node = objectMapper .readTree(response.getBody()); return
		 * node.at("/content/0/text") .asText().trim(); }
		 * 
		 * private String callOpenAI(String prompt) throws Exception { HttpHeaders
		 * headers = new HttpHeaders();
		 * headers.setContentType(MediaType.APPLICATION_JSON);
		 * headers.setBearerAuth(apiKey);
		 * 
		 * Map<String, Object> body = new HashMap<>(); body.put("model", model);
		 * body.put("max_tokens", 200); body.put("messages", List.of( Map.of("role",
		 * "user", "content", prompt) ));
		 * 
		 * HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
		 * 
		 * ResponseEntity<String> response = restTemplate.exchange( apiUrl,
		 * HttpMethod.POST, entity, String.class);
		 * 
		 * JsonNode node = objectMapper .readTree(response.getBody()); return node.at(
		 * "/choices/0/message/content") .asText().trim(); }
		 * 
		 * // ───────────────────────────────────────────── // RESPONSE PARSERS //
		 * ─────────────────────────────────────────────
		 * 
		 * private String extractIntent(String llmResponse) { String cleaned =
		 * llmResponse .toLowerCase().trim();
		 * 
		 * // Find matching intent in known list for (String intent : KNOWN_INTENTS) {
		 * if (cleaned.contains(intent)) { return intent; } } return "general"; }
		 * 
		 * private LLMResult parseCombinedResponse( String response) { try { // Clean
		 * JSON from response String json = response .replaceAll("```json", "")
		 * .replaceAll("```", "") .trim();
		 * 
		 * JsonNode node = objectMapper.readTree(json);
		 * 
		 * String intent = node.get("intent").asText("general"); boolean needsDB =
		 * node.get("needsDB").asBoolean(false); String directAnswer =
		 * node.has("directAnswer") && !node.get("directAnswer").isNull() ?
		 * node.get("directAnswer").asText() : null;
		 * 
		 * return new LLMResult( intent, needsDB, directAnswer);
		 * 
		 * } catch (Exception e) { log.error("Failed to parse LLM response: {}",
		 * response); return new LLMResult( "general", false, "I am here to help! " +
		 * "What can I assist you with?"); } }
		 * 
		 * private String getRoleName(String appId) { return switch (appId) { case
		 * "VENDOR" -> "vendor/restaurant"; case "RIDER" -> "delivery rider"; default ->
		 * "customer"; }; }
		 * 
		 * // Result holder public record LLMResult( String intent, boolean needsDB,
		 * String directAnswer) {} }
		 */  


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
 * private final RestTemplate restTemplate = new RestTemplate();
 * 
 * private final ObjectMapper objectMapper = new ObjectMapper();
 * 
 * // All known intents in your system private static final List<String>
 * KNOWN_INTENTS = List.of( "track_order", "order_status", "order_history",
 * "order_timeline", "cancel_order", "delivery_delay", "order_not_delivered",
 * "payment_failed", "money_deducted", "refund_status", "payment_pending",
 * "missing_item", "wrong_item", "damaged_item", "login_issue",
 * "app_not_working", "talk_to_agent", "payout_status", "payout_not_received",
 * "vendor_cancel_order", "new_order", "rider_not_arrived", "current_order",
 * "earnings_today", "payout_rider", "payment_not_received_rider", "emergency"
 * );
 * 
 * public record LLMResult( String intent, boolean needsDB, String directAnswer)
 * {}
 * 
 * // ───────────────────────────────────────────── // MAIN METHOD — called from
 * ChatServiceImpl // ───────────────────────────────────────────── public
 * LLMResult processMessage( String userMessage, String userName, String appId)
 * {
 * 
 * log.info("LLM processing → message: '{}' | " + "appId: {}", userMessage,
 * appId);
 * 
 * try { String rawResponse = callLLM( userMessage, userName, appId);
 * 
 * log.info("LLM raw response: {}", rawResponse);
 * 
 * LLMResult result = parseResponse( rawResponse, userMessage);
 * 
 * log.info("LLM result → intent: {} | " + "needsDB: {}", result.intent(),
 * result.needsDB());
 * 
 * return result;
 * 
 * } catch (Exception e) { log.error("LLM failed: {}", e.getMessage()); //
 * Fallback — try keyword match return keywordFallback(userMessage); } }
 * 
 * // ───────────────────────────────────────────── // CALL LLM API //
 * ───────────────────────────────────────────── private String callLLM( String
 * userMessage, String userName, String appId) throws Exception {
 * 
 * String prompt = buildPrompt( userMessage, userName, appId);
 * 
 * if ("claude".equalsIgnoreCase(provider)) { return callClaude(prompt); } else
 * { return callOpenAI(prompt); } }
 * 
 * private String callClaude(String prompt) throws Exception {
 * 
 * HttpHeaders headers = new HttpHeaders(); headers.setContentType(
 * MediaType.APPLICATION_JSON); headers.set("x-api-key", apiKey);
 * headers.set("anthropic-version", "2023-06-01");
 * 
 * Map<String, Object> body = new HashMap<>(); body.put("model", model);
 * body.put("max_tokens", 300); body.put("messages", List.of( Map.of("role",
 * "user", "content", prompt) ));
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
 * body.put("max_tokens", 300); body.put("messages", List.of( Map.of("role",
 * "user", "content", prompt) ));
 * 
 * HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
 * 
 * ResponseEntity<String> response = restTemplate.exchange( apiUrl,
 * HttpMethod.POST, entity, String.class);
 * 
 * JsonNode node = objectMapper .readTree(response.getBody()); return node.at(
 * "/choices/0/message/content") .asText().trim(); }
 * 
 * // ───────────────────────────────────────────── // BUILD PROMPT // Very
 * explicit — forces LLM to return exact JSON //
 * ───────────────────────────────────────────── private String buildPrompt(
 * String userMessage, String userName, String appId) {
 * 
 * String role = switch (appId) { case "VENDOR" -> "restaurant vendor"; case
 * "RIDER" -> "delivery rider"; default -> "customer"; };
 * 
 * return """ You are a support chatbot classifier for a food delivery app. User
 * name: %s User role: %s
 * 
 * KNOWN INTENTS (pick one if message matches): %s
 * 
 * USER MESSAGE: "%s"
 * 
 * TASK: 1. If the message matches a known intent, return:
 * {"intent":"<matched_intent>","needsDB":true,"directAnswer":null}
 * 
 * 2. If the message is a greeting, thanks, or general chat, return:
 * {"intent":"general","needsDB":false,
 * "directAnswer":"<your short friendly reply>"}
 * 
 * RULES: - Return ONLY valid JSON. No extra text. No markdown. No explanation.
 * - Do not wrap in code blocks. - directAnswer must be null when needsDB is
 * true. - Keep directAnswer under 20 words if provided.
 * 
 * JSON:""".formatted( userName, role, String.join("\n", KNOWN_INTENTS),
 * userMessage); }
 * 
 * // ───────────────────────────────────────────── // PARSE LLM RESPONSE //
 * Handles messy LLM output safely //
 * ───────────────────────────────────────────── private LLMResult
 * parseResponse( String rawResponse, String userMessage) {
 * 
 * // Step 1 — clean the response String cleaned = rawResponse
 * .replaceAll("(?s)```json", "") .replaceAll("(?s)```", "") .trim();
 * 
 * // Step 2 — extract JSON block // sometimes LLM adds text before/after JSON
 * Pattern jsonPattern = Pattern.compile( "\\{[^{}]*\\}", Pattern.DOTALL);
 * Matcher matcher = jsonPattern.matcher(cleaned);
 * 
 * if (matcher.find()) { cleaned = matcher.group(); }
 * 
 * log.info("Cleaned LLM response: {}", cleaned);
 * 
 * try { JsonNode node = objectMapper.readTree(cleaned);
 * 
 * String intent = node.has("intent") ? node.get("intent").asText("general") :
 * "general";
 * 
 * boolean needsDB = node.has("needsDB") ? node.get("needsDB").asBoolean(false)
 * : KNOWN_INTENTS.contains(intent);
 * 
 * String directAnswer = null; if (node.has("directAnswer") &&
 * !node.get("directAnswer") .isNull()) { directAnswer =
 * node.get("directAnswer").asText(); }
 * 
 * // Validate — if intent not in known list // and not general → treat as
 * general if (!KNOWN_INTENTS.contains(intent) && !"general".equals(intent)) {
 * log.warn("Unknown intent from LLM: {}", intent); intent = "general"; needsDB
 * = false; }
 * 
 * return new LLMResult( intent, needsDB, directAnswer);
 * 
 * } catch (Exception e) { log.error("JSON parse failed for: {} | " +
 * "error: {}", cleaned, e.getMessage());
 * 
 * // Final fallback — keyword match return keywordFallback(userMessage); } }
 * 
 * // ───────────────────────────────────────────── // KEYWORD FALLBACK // Used
 * when LLM fails or returns bad JSON // Maps common words to intents directly
 * // ───────────────────────────────────────────── private LLMResult
 * keywordFallback( String message) {
 * 
 * String lower = message.toLowerCase().trim();
 * 
 * log.info("Using keyword fallback for: {}", lower);
 * 
 * // Greeting patterns if (lower.matches(
 * ".*(hi|hello|hey|hii|helo|good morning" + "|good evening|namaste).*")) {
 * return new LLMResult( "general", false, "Hi! I am your support assistant. " +
 * "How can I help you?"); }
 * 
 * // Thanks patterns if (lower.matches( ".*(thank|thanks|ok|okay|got it" +
 * "|understood|cool|great).*")) { return new LLMResult( "general", false,
 * "You are welcome! " + "Is there anything else I can help you with?"); }
 * 
 * // Order tracking if (lower.matches(
 * ".*(where.*order|track.*order|order.*status" + "|order.*where|kaha.*order" +
 * "|order.*kaha).*")) { return new LLMResult( "track_order", true, null); }
 * 
 * // Refund if (lower.matches( ".*(refund|money back|return.*money" +
 * "|paisa.*wapas|wapas.*paisa).*")) { return new LLMResult( "refund_status",
 * true, null); }
 * 
 * // Cancel if (lower.matches( ".*(cancel|cancellation|band karo" +
 * "|cancel.*order|order.*cancel).*")) { return new LLMResult( "cancel_order",
 * true, null); }
 * 
 * // Payment failed if (lower.matches( ".*(payment.*fail|fail.*payment" +
 * "|payment.*not.*done|money.*deduct" + "|deduct.*money).*")) { return new
 * LLMResult( "payment_failed", true, null); }
 * 
 * // Late delivery if (lower.matches( ".*(late|delay|too long|taking long" +
 * "|slow|der ho rahi|der.*delivery).*")) { return new LLMResult(
 * "delivery_delay", true, null); }
 * 
 * // Missing item if (lower.matches( ".*(missing|item.*missing|not.*received" +
 * "|item.*not|wrong.*item" + "|incorrect.*item).*")) { return new LLMResult(
 * "missing_item", false, null); }
 * 
 * // Talk to agent if (lower.matches( ".*(agent|support|human|talk.*someone" +
 * "|speak.*someone|help).*")) { return new LLMResult( "talk_to_agent", false,
 * null); }
 * 
 * // Default — ask LLM directly but as general return new LLMResult( "general",
 * false, "I am not sure I understood that. " +
 * "Could you please rephrase your question?"); } }
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

    // ─────────────────────────────────────────────
    // KEYWORD MAP — simple contains check
    // Most reliable — no regex complexity
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

    // Intents that need DB query
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

    // General / conversational keywords
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
    // MAIN METHOD
    // ─────────────────────────────────────────────
    public LLMResult processMessage(
            String userMessage,
            String userName,
            String appId) {

        String lower = userMessage
            .toLowerCase().trim();

        log.info("Processing message: '{}'", lower);

        // STEP 1 — Check greeting first (fastest)
        if (isGreeting(lower)) {
            String reply = "Hi " + userName
                + "! I am your support assistant. "
                + "How can I help you today?";
            log.info("Greeting detected");
            return new LLMResult(
                "general", false, reply);
        }

        // STEP 2 — Check thanks/closing
        if (isThanks(lower)) {
            String reply = "You are welcome"
                + (userName.equals("there")
                    ? "" : " " + userName)
                + "! Is there anything else "
                + "I can help you with?";
            log.info("Thanks detected");
            return new LLMResult(
                "general", false, reply);
        }

        // STEP 3 — Keyword map match (most reliable)
        String keywordIntent =
            matchKeyword(lower);

        if (keywordIntent != null) {
            log.info("Keyword match → intent: {}",
                     keywordIntent);
            boolean needsDB =
                NEEDS_DB_INTENTS.contains(
                    keywordIntent);
            return new LLMResult(
                keywordIntent, needsDB, null);
        }

        // STEP 4 — LLM for complex messages
        // only if keyword match failed
        log.info("No keyword match — calling LLM");
        try {
            return callLLMForIntent(
                lower, userName, appId);
        } catch (Exception e) {
            log.error("LLM failed: {}", e.getMessage());
            // STEP 5 — Final fallback
            return new LLMResult(
                "talk_to_agent", false, null);
        }
    }

    // ─────────────────────────────────────────────
    // KEYWORD MATCHING — simple and reliable
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
        // Exact match for single words
        if (GREETING_WORDS.contains(lower))
            return true;
        // Contains check
        for (String word : GREETING_WORDS) {
            if (lower.startsWith(word + " ")
                    || lower.equals(word)) {
                return true;
            }
        }
        return false;
    }

    private boolean isThanks(String lower) {
        for (String word : THANKS_WORDS) {
            if (lower.equals(word)
                    || lower.startsWith(word + " ")
                    || lower.contains(" " + word)) {
                return true;
            }
        }
        return false;
    }

    // ─────────────────────────────────────────────
    // LLM CALL — only for unmatched messages
    // ─────────────────────────────────────────────
    private LLMResult callLLMForIntent(
            String message,
            String userName,
            String appId) throws Exception {

        String prompt = """
You are a support chatbot for a food delivery app.
User: %s, Role: %s

Classify this message into ONE of these intents:
track_order, order_history, order_timeline,
cancel_order, delivery_delay, order_not_delivered,
payment_failed, money_deducted, refund_status,
payment_pending, missing_item, wrong_item,
damaged_item, login_issue, app_not_working,
talk_to_agent, payout_status, new_order,
rider_not_arrived, current_order, earnings_today,
emergency, general

Message: "%s"

Reply with ONLY this JSON, nothing else:
{"intent":"<intent>","needsDB":<true/false>,"directAnswer":<null or "short reply if general">}
""".formatted(getRoleName(appId), userName, message);

        String raw;
        if ("claude".equalsIgnoreCase(provider)) {
            raw = callClaude(prompt);
        } else {
            raw = callOpenAI(prompt);
        }

        log.info("LLM raw: {}", raw);
        return parseJSON(raw, message);
    }

    // ─────────────────────────────────────────────
    // JSON PARSER
    // ─────────────────────────────────────────────
    private LLMResult parseJSON(
            String raw, String fallbackMessage) {

        try {
            // Extract JSON from response
            String cleaned = raw
                .replaceAll("(?s)```json", "")
                .replaceAll("(?s)```", "")
                .trim();

            // Find JSON object
            int start = cleaned.indexOf('{');
            int end   = cleaned.lastIndexOf('}');

            if (start == -1 || end == -1) {
                log.warn("No JSON found in: {}", cleaned);
                return keywordFallbackOrAgent(
                    fallbackMessage);
            }

            cleaned = cleaned.substring(start, end + 1);
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

    // When everything fails — escalate to agent
    // Better than "I don't understand"
    private LLMResult keywordFallbackOrAgent(
            String message) {

        // Try keyword one more time
        String intent = matchKeyword(
            message.toLowerCase().trim());

        if (intent != null) {
            return new LLMResult(
                intent,
                NEEDS_DB_INTENTS.contains(intent),
                null);
        }

        // Give up — route to support agent
        // This raises a ticket instead of
        // saying "I don't understand"
        return new LLMResult(
            "talk_to_agent", false, null);
    }

    // ─────────────────────────────────────────────
    // HELPERS
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