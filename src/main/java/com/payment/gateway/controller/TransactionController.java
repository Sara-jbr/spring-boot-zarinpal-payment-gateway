package com.payment.gateway.controller;

import com.payment.gateway.entity.PaymentRequest;
import com.payment.gateway.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("api")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/pay")
    public ResponseEntity<Map<String, String>> requestPayment(@RequestBody PaymentRequest paymentRequest) {
        return transactionService.processPaymentRequest(paymentRequest);
    }

    @GetMapping("/verify")
    public String verifyPayment(@RequestParam("Authority") String authority,
                                @RequestParam("Status") String status,
                                Model model) {
        return transactionService.verifyPayment(authority, status, model);
    }
}