package com.payment.gateway.service;

import com.payment.gateway.entity.PaymentRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.util.Map;

public interface TransactionService {
    ResponseEntity<Map<String, String>> processPaymentRequest(PaymentRequest paymentRequest);
    String verifyPayment(String authority, String status, Model model);
}
