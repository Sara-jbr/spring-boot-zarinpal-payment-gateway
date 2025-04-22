package com.payment.gateway.service;

public interface EmailService {
    void sendPaymentSuccessEmail(String to, String refId, Integer amount);
    void sendPaymentFailedEmail(String to, String reason);
    void sendPaymentLinkEmail(String to, String paymentUrl);

}