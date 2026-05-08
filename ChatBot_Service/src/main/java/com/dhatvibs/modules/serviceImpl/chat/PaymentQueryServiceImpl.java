package com.dhatvibs.modules.serviceImpl.chat;


import com.dhatvibs.modules.entities.auth.CbUser;
import com.dhatvibs.modules.entities.chat.CbPayment;
import com.dhatvibs.modules.repository.auth.CbUserRepository;
import com.dhatvibs.modules.repository.chat.CbPaymentRepository;
import com.dhatvibs.modules.service.chat.PaymentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentQueryServiceImpl
        implements PaymentQueryService {

    private final CbPaymentRepository paymentRepo;
    private final CbUserRepository    userRepo;

    @Override
    public String getRefundStatus(
            String externalUserId,
            String appId,
            UUID contextOrderId) {

        if (contextOrderId != null) {
            Optional<CbPayment> payment =
                paymentRepo.findByCbOrderId(contextOrderId);
            if (payment.isPresent())
                return buildRefundReply(payment.get());
            return "No payment found for this order.";
        }

        CbUser user = findUser(externalUserId, appId);
        if (user == null) return "User not found.";

        List<CbPayment> refunds =
            paymentRepo.findRefundsByUserId(user.getId());
        if (refunds.isEmpty())
            return "No refunds found for your account.";

        return buildRefundReply(refunds.get(0));
    }

    @Override
    public String getPaymentFailureStatus(
            String externalUserId,
            String appId,
            UUID contextOrderId) {

        if (contextOrderId != null) {
            Optional<CbPayment> payment =
                paymentRepo.findByCbOrderId(contextOrderId);
            if (payment.isPresent()) {
                CbPayment p = payment.get();
                return "Payment Status: "
                     + p.getPaymentStatus() + "\n"
                     + "Amount: Rs." + p.getAmount() + "\n"
                     + "Reason: "
                     + (p.getFailureReason() != null
                         ? p.getFailureReason()
                         : "Unknown") + "\n"
                     + "If money deducted, "
                     + "refund in 5-7 business days.";
            }
        }

        CbUser user = findUser(externalUserId, appId);
        if (user == null) return "User not found.";

        List<CbPayment> failed =
            paymentRepo.findFailedPaymentsByUserId(
                user.getId());
        if (failed.isEmpty())
            return "No failed payments found.";

        CbPayment p = failed.get(0);
        return "Payment FAILED\n"
             + "Amount: Rs." + p.getAmount() + "\n"
             + "Reason: " + (p.getFailureReason() != null
                 ? p.getFailureReason() : "Unknown");
    }

    @Override
    public String getLatestPaymentStatus(
            String externalUserId,
            String appId,
            UUID contextOrderId) {

        if (contextOrderId != null) {
            Optional<CbPayment> payment =
                paymentRepo.findByCbOrderId(contextOrderId);
            if (payment.isPresent()) {
                CbPayment p = payment.get();
                return "Payment: Rs." + p.getAmount() + "\n"
                     + "Status: " + p.getPaymentStatus()
                     + "\nMethod: " + p.getPaymentMethod();
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
        return "Latest Payment\n"
             + "Amount: Rs." + p.getAmount() + "\n"
             + "Status: " + p.getPaymentStatus() + "\n"
             + "Method: " + p.getPaymentMethod();
    }

    private String buildRefundReply(CbPayment p) {
        String status = p.getPaymentStatus();
        StringBuilder sb = new StringBuilder();
        sb.append("Refund Status\n");
        sb.append("Amount: Rs.")
          .append(p.getRefundAmount()).append("\n");
        sb.append("Status: ").append(status).append("\n");

        switch (status) {
            case "REFUNDED" ->
                sb.append("Refund processed on ")
                  .append(p.getRefundedAt() != null
                      ? p.getRefundedAt().toLocalDate()
                      : "N/A").append("\n");
            case "REFUND_INITIATED",
                 "REFUND_PROCESSING" ->
                sb.append("Refund in progress. "
                        + "Expected 5-7 business days.\n");
            case "REFUND_FAILED" ->
                sb.append("Refund failed. "
                        + "Team will contact you.\n");
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