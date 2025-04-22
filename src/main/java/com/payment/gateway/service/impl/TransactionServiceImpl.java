package com.payment.gateway.service.impl;

import com.payment.gateway.entity.PaymentRequest;
import com.payment.gateway.entity.Transaction;
import com.payment.gateway.repository.TransactionRepository;
import com.payment.gateway.service.EmailService;
import com.payment.gateway.service.TransactionService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final EmailService emailService;

    public TransactionServiceImpl(TransactionRepository transactionRepository, EmailService emailService) {
        this.transactionRepository = transactionRepository;
        this.emailService = emailService;
    }

    @Override
    public ResponseEntity<Map<String, String>> processPaymentRequest(PaymentRequest paymentRequest) {
        String url = "https://sandbox.zarinpal.com/pg/v4/payment/request.json";

        Map<String, Object> payload = new HashMap<>();
        payload.put("merchant_id", paymentRequest.getMerchantId());
        payload.put("amount", paymentRequest.getAmount());
        payload.put("callback_url", paymentRequest.getCallbackUrl());
        payload.put("description", paymentRequest.getDescription());

        Map<String, String> metadata = new HashMap<>();
        PaymentRequest.Metadata md = paymentRequest.getMetadata();
        if (md != null) {
            if (md.getMobile() != null) metadata.put("mobile", md.getMobile());
            if (md.getEmail() != null) metadata.put("email", md.getEmail());
            if (md.getOrderId() != null) metadata.put("order_id", md.getOrderId());
        }
        payload.put("metadata", metadata);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        Map<String, Object> body = response.getBody();
        if (body != null && body.get("data") != null) {
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            if ((int) data.get("code") == 100) {
                String authority = (String) data.get("authority");
                String paymentUrl = "https://sandbox.zarinpal.com/pg/StartPay/" + authority;

                Transaction transaction = new Transaction();
                transaction.setMerchantId(paymentRequest.getMerchantId());
                transaction.setAmount(paymentRequest.getAmount());
                transaction.setAuthority(authority);
                transaction.setStatus("PENDING");
                transaction.setCallbackUrl(paymentRequest.getCallbackUrl());
                transaction.setDescription(paymentRequest.getDescription());

                if (md != null && md.getEmail() != null) {
                    transaction.setEmail(md.getEmail());
                }

                transactionRepository.save(transaction);

                if (transaction.getEmail() != null) {
                    emailService.sendPaymentLinkEmail(transaction.getEmail(), paymentUrl);
                }

                Map<String, String> result = new HashMap<>();
                result.put("payment_url", paymentUrl);
                return ResponseEntity.ok(result);
            }
        }

        Map<String, String> error = new HashMap<>();
        error.put("message", "Payment request failed.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @Override
    public String verifyPayment(String authority, String status, Model model) {
        Optional<Transaction> transactionOpt = transactionRepository.findByAuthority(authority);
        if (transactionOpt.isEmpty()) {
            model.addAttribute("message", "تراکنش مورد نظر یافت نشد.");
            return "payment-failed";
        }

        Transaction transaction = transactionOpt.get();

        if (!"OK".equals(status)) {
            transaction.setStatus("FAILED");
            transactionRepository.save(transaction);

            if (transaction.getEmail() != null) {
                emailService.sendPaymentFailedEmail(transaction.getEmail(), "پرداخت توسط کاربر لغو شد.");
            }

            model.addAttribute("message", "پرداخت توسط کاربر لغو شد.");
            return "payment-failed";
        }

        String url = "https://sandbox.zarinpal.com/pg/v4/payment/verify.json";
        Map<String, Object> payload = new HashMap<>();
        payload.put("merchant_id", transaction.getMerchantId());
        payload.put("amount", transaction.getAmount());
        payload.put("authority", authority);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        Map<String, Object> body = response.getBody();
        if (body != null && body.get("data") != null) {
            Map<String, Object> data = (Map<String, Object>) body.get("data");

            if ((int) data.get("code") == 100) {
                Long refId = ((Number) data.get("ref_id")).longValue();
                transaction.setRefId(refId.toString());
                transaction.setStatus("PAID");
                transactionRepository.save(transaction);

                if (transaction.getEmail() != null) {
                    emailService.sendPaymentSuccessEmail(transaction.getEmail(), refId.toString(), transaction.getAmount());
                }

                model.addAttribute("message", "پرداخت با موفقیت انجام شد.");
                model.addAttribute("refId", refId);
                model.addAttribute("amount", transaction.getAmount());
                model.addAttribute("email", transaction.getEmail());
                return "payment-success";
            }
        }

        transaction.setStatus("FAILED");
        transactionRepository.save(transaction);

        if (transaction.getEmail() != null) {
            emailService.sendPaymentFailedEmail(transaction.getEmail(), "پرداخت توسط درگاه ناموفق بود.");
        }

        model.addAttribute("message", "تأیید پرداخت با شکست مواجه شد.");
        return "payment-failed";
    }
}