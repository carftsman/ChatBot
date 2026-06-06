package com.dhatvibs.modules.vendor.serviceImpl;

import com.dhatvibs.modules.vendor.dto.*;
import com.dhatvibs.modules.vendor.entity.*;
import com.dhatvibs.modules.vendor.respository.*;
import com.dhatvibs.modules.vendor.service.*;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorFaqServiceImpl implements VendorFaqService {

	private final VendorFaqRepository faqRepo;
	private final VendorChatMessageRepository messageRepo;
	private final VendorTicketRepository ticketRepo;
	private final VendorChatSessionRepository sessionRepo;
	private final VendorQueryService queryService;

	@Override
	public List<VendorCategoryResponse> getCategories() {
		return faqRepo.findAllCategories().stream()
				.map(f -> VendorCategoryResponse.builder().category(f.getCategory()).displayName(f.getQuestion())
						.displayOrder(f.getDisplayOrder() != null ? f.getDisplayOrder() : 0).build())
				.collect(Collectors.toList());
	}

	@Override
	public List<VendorFaqQuestionResponse> getQuestions(String category) {
		return faqRepo.findQuestionsByCategory(category).stream()
				.map(f -> VendorFaqQuestionResponse.builder().faqId(f.getId()).question(f.getQuestion())
						.category(f.getCategory()).intent(f.getIntent())
						.displayOrder(f.getDisplayOrder() != null ? f.getDisplayOrder() : 0).build())
				.collect(Collectors.toList());
	}

	@Override
	public VendorFaqAnswerResponse getAnswer(UUID faqId, UUID sessionId) {

		VendorFaq faq = faqRepo.findById(faqId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

		String answer = null;
		boolean needsTicket = false;
		String ticketId = null;

		String vendorToken = null;
		String contextOrderId = null;
		String vendorId = null;

		if (sessionId != null) {
			Optional<VendorChatSession> session = sessionRepo.findById(sessionId);
			if (session.isPresent()) {
				vendorToken = session.get().getVendorToken();
				contextOrderId = session.get().getContextOrderId();
				vendorId = session.get().getVendorId();
			}
		}

		if (isSupportIntent(faq.getIntent())) {
			VendorTicket ticket = raiseTicket(sessionId, vendorId, contextOrderId, faq.getQuestion(),
					faq.getCategory());

			answer = "Support ticket #" + ticket.getId().toString().substring(0, 8).toUpperCase() + " created.\n"
					+ "Our team will contact you " + "within 4 hours.";
			needsTicket = true;
			ticketId = ticket.getId().toString();

		} else if (Boolean.TRUE.equals(faq.getNeedsApi())) {
			answer = resolveFromApi(faq.getIntent(), vendorToken, contextOrderId);
		} else {
			answer = faq.getAnswer();
		}

		if (sessionId != null) {
			saveMessage(sessionId, "USER", faq.getQuestion(), faq.getIntent(), faqId, "FAQ");
			saveMessage(sessionId, "BOT", answer, faq.getIntent(), faqId, "FAQ");
		}

		return VendorFaqAnswerResponse.builder().faqId(faq.getId()).question(faq.getQuestion()).answer(answer)
				.intent(faq.getIntent()).category(faq.getCategory()).needsTicket(needsTicket).ticketId(ticketId)
				.build();
	}

	
	
	private String resolveFromApi(
	        String intent, String token,
	        String orderId) {

	    if (token == null)
	        return "Session expired. Login again.";

	    return switch (intent) {
		
	    
	    case "pending_orders" -> {
	        List<Map> orders =
	            queryService.getPendingOrders(token);
	        yield orders.isEmpty()
	            ? "No pending orders."
	            : orders.size() + " pending orders.";
	    }
	    case "delivered_orders" -> {
	        List<Map> orders =
	            queryService.getDeliveredOrders(token);
	        yield orders.isEmpty()
	            ? "No delivered orders."
	            : orders.size() + " delivered orders.";
	    }
	    case "cancelled_orders" -> {
	        List<Map> orders =
	            queryService.getCancelledOrders(token);
	        yield orders.isEmpty()
	            ? "No cancelled orders."
	            : orders.size() + " cancelled orders.";
	    }
	        case "order_details" ->
	            queryService.getOrderDetails(
	                orderId, token);
		/*
		 * case "store_status", "store_details" -> queryService.getStoreDetails(token);
		 */
	        case "store_status",
	        "store_details" -> {
	       // Dashboard API gives order stats + rating
	       // Use it to show store summary
	       yield queryService.getStoreDetails(token);
	   }
	        case "payout_status",
	             "wallet_balance" ->
	            queryService.getWalletBalance(token);
	        case "transaction_history" ->
	            queryService.getTransactionHistory(token);
	        default ->
	            "I am checking this for you.";
	    };
	}

	private boolean isSupportIntent(String intent) {
		return switch (intent) {
		case "talk_to_agent", "emergency", "fallback", "login_issue" -> true;
		default -> false;
		};
	}

	private VendorTicket raiseTicket(UUID sessionId, String vendorId, String orderId, String description,
			String category) {
		return ticketRepo.save(VendorTicket.builder().sessionId(sessionId).vendorId(vendorId).orderId(orderId)
				.category(category).description(description).status("OPEN").createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).build());
	}

	private void saveMessage(UUID sessionId, String senderType, String message, String intent, UUID faqId,
			String messageType) {
		messageRepo.save(VendorChatMessage.builder().sessionId(sessionId).senderType(senderType).message(message)
				.intent(intent).matchedFaqId(faqId).messageType(messageType).sentAt(LocalDateTime.now()).build());
	}
}