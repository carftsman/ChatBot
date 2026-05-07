package com.dhatvibs.modules.service.chat;

/*
 * import lombok.RequiredArgsConstructor; import
 * org.springframework.stereotype.Service;
 * 
 * import com.dhatvibs.modules.entities.auth.CbUser; import
 * com.dhatvibs.modules.entities.chat.CbPayment; import
 * com.dhatvibs.modules.repository.auth.CbUserRepository; import
 * com.dhatvibs.modules.repository.chat.*;
 * 
 * import java.util.*;
 * 
 * @Service
 * 
 * @RequiredArgsConstructor public class PaymentQueryService {
 * 
 * private final CbPaymentRepository paymentRepo; private final CbUserRepository
 * userRepo; private final CbOrderRepository orderRepo;
 * 
 * public String getRefundStatus( String externalUserId, String appId) {
 * Optional<CbUser> user = userRepo .findByExternalUserIdAndAppId(
 * externalUserId, appId); if (user.isEmpty()) return "User not found.";
 * 
 * List<CbPayment> refunds = paymentRepo.findRefundsByUserId(
 * user.get().getId());
 * 
 * if (refunds.isEmpty()) return "No refunds found for your account.";
 * 
 * CbPayment latest = refunds.get(0); return buildRefundReply(latest); }
 * 
 * public String getPaymentFailureStatus( String externalUserId, String appId) {
 * Optional<CbUser> user = userRepo .findByExternalUserIdAndAppId(
 * externalUserId, appId); if (user.isEmpty()) return "User not found.";
 * 
 * List<CbPayment> failed = paymentRepo.findFailedPaymentsByUserId(
 * user.get().getId());
 * 
 * if (failed.isEmpty()) return "No failed payments found. " +
 * "Your recent payments are all successful.";
 * 
 * CbPayment p = failed.get(0); return "💳 Payment Status: FAILED\n" +
 * "Amount: ₹" + p.getAmount() + "\n" + "Reason: " + (p.getFailureReason() !=
 * null ? p.getFailureReason() : "Unknown") + "\n" +
 * "If your money was deducted, " +
 * "it will be refunded within 5-7 business days."; }
 * 
 * public String getLatestPaymentStatus( String externalUserId, String appId) {
 * Optional<CbUser> user = userRepo .findByExternalUserIdAndAppId(
 * externalUserId, appId); if (user.isEmpty()) return "User not found.";
 * 
 * List<CbPayment> payments = paymentRepo.findByCbUserIdOrderByCreatedAtDesc(
 * user.get().getId());
 * 
 * if (payments.isEmpty()) return "No payment records found.";
 * 
 * CbPayment p = payments.get(0); return "💳 Latest Payment\n" + "Amount: ₹" +
 * p.getAmount() + "\n" + "Status: " + p.getPaymentStatus() + "\n" + "Method: "
 * + p.getPaymentMethod() + "\n" + "Gateway: " + p.getGateway(); }
 * 
 * private String buildRefundReply(CbPayment p) { String status =
 * p.getPaymentStatus(); StringBuilder sb = new StringBuilder();
 * sb.append("💰 Refund Status\n");
 * sb.append("Amount: ₹").append(p.getRefundAmount()) .append("\n");
 * sb.append("Status: ").append(status).append("\n");
 * 
 * if ("REFUNDED".equals(status)) { sb.append("✅ Refund has been processed!\n");
 * if (p.getRefundedAt() != null) { sb.append("Refunded on: ")
 * .append(p.getRefundedAt() .toLocalDate()) .append("\n"); } } else if
 * ("REFUND_INITIATED".equals(status) || "REFUND_PROCESSING".equals(status)) {
 * sb.append("⏳ Refund is in progress.\n");
 * sb.append("Expected: 5-7 business days " +
 * "to your original payment method.\n"); } else if
 * ("REFUND_FAILED".equals(status)) { sb.append("❌ Refund failed. " +
 * "Our team will contact you shortly.\n"); } return sb.toString(); } }
 */ 


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.dhatvibs.modules.entities.auth.CbUser;
import com.dhatvibs.modules.entities.chat.CbPayment;
import com.dhatvibs.modules.repository.auth.CbUserRepository;
import com.dhatvibs.modules.repository.chat.CbPaymentRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentQueryService {

    private final CbPaymentRepository paymentRepo;
    private final CbUserRepository    userRepo;

    // Get refund — uses contextOrderId if available
    public String getRefundStatus(
            String externalUserId,
            String appId,
            UUID contextOrderId) {

        if (contextOrderId != null) {
            // Direct lookup by orderId — most accurate
            Optional<CbPayment> payment =
                paymentRepo.findByCbOrderId(contextOrderId);
            if (payment.isPresent()) {
                return buildRefundReply(payment.get());
            }
            return "No payment found for this order.";
        }

        // Fallback — latest refund by userId
        CbUser user = findUser(externalUserId, appId);
        if (user == null) return "User not found.";

        List<CbPayment> refunds =
            paymentRepo.findRefundsByUserId(user.getId());
        if (refunds.isEmpty())
            return "No refunds found for your account.";

        return buildRefundReply(refunds.get(0));
    }

    // Get payment failure — uses contextOrderId if available
    public String getPaymentFailureStatus(
            String externalUserId,
            String appId,
            UUID contextOrderId) {

        if (contextOrderId != null) {
            Optional<CbPayment> payment =
                paymentRepo.findByCbOrderId(contextOrderId);
            if (payment.isPresent()) {
                CbPayment p = payment.get();
                return "💳 Payment Status: "
                     + p.getPaymentStatus() + "\n"
                     + "Amount: ₹" + p.getAmount() + "\n"
                     + "Reason: "
                     + (p.getFailureReason() != null
                         ? p.getFailureReason()
                         : "Unknown") + "\n"
                     + "If money was deducted, "
                     + "it will be refunded in 5-7 days.";
            }
        }

        CbUser user = findUser(externalUserId, appId);
        if (user == null) return "User not found.";

        List<CbPayment> failed =
            paymentRepo.findFailedPaymentsByUserId(
                user.getId());
        if (failed.isEmpty())
            return "No failed payments found. "
                 + "Your recent payments are successful.";

        CbPayment p = failed.get(0);
        return "💳 Payment: FAILED\n"
             + "Amount: ₹" + p.getAmount() + "\n"
             + "Reason: " + (p.getFailureReason() != null
                 ? p.getFailureReason() : "Unknown");
    }

    // Get latest payment — uses contextOrderId if available
    public String getLatestPaymentStatus(
            String externalUserId,
            String appId,
            UUID contextOrderId) {

        if (contextOrderId != null) {
            Optional<CbPayment> payment =
                paymentRepo.findByCbOrderId(contextOrderId);
            if (payment.isPresent()) {
                CbPayment p = payment.get();
                return "💳 Payment\n"
                     + "Amount: ₹" + p.getAmount() + "\n"
                     + "Status: " + p.getPaymentStatus()
                     + "\n"
                     + "Method: " + p.getPaymentMethod()
                     + "\n"
                     + "Gateway: " + p.getGateway();
            }
        }

        CbUser user = findUser(externalUserId, appId);
        if (user == null) return "User not found.";

        List<CbPayment> payments =
            paymentRepo.findByCbUserIdOrderByCreatedAtDesc(
                user.getId());
        if (payments.isEmpty())
            return "No payment records found.";

        CbPayment p = payments.get(0);
        return "💳 Latest Payment\n"
             + "Amount: ₹" + p.getAmount() + "\n"
             + "Status: " + p.getPaymentStatus() + "\n"
             + "Method: " + p.getPaymentMethod();
    }

    private String buildRefundReply(CbPayment p) {
        String status = p.getPaymentStatus();
        StringBuilder sb = new StringBuilder();
        sb.append("💰 Refund Status\n");
        sb.append("Amount: ₹")
          .append(p.getRefundAmount()).append("\n");
        sb.append("Status: ").append(status).append("\n");

        switch (status) {
            case "REFUNDED" ->
                sb.append("✅ Refund processed on ")
                  .append(p.getRefundedAt() != null
                      ? p.getRefundedAt().toLocalDate()
                      : "N/A")
                  .append("\n");
            case "REFUND_INITIATED",
                 "REFUND_PROCESSING" ->
                sb.append("⏳ Refund in progress.\n"
                        + "Expected: 5-7 business days.\n");
            case "REFUND_FAILED" ->
                sb.append("❌ Refund failed. "
                        + "Our team will contact you.\n");
        }
        return sb.toString();
    }

    private CbUser findUser(
            String externalUserId, String appId) {
        return userRepo
            .findByExternalUserIdAndAppId(
                externalUserId, appId)
            .orElse(null);
    }
}