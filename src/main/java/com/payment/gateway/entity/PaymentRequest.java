package com.payment.gateway.entity;

import lombok.Data;

@Data
public class PaymentRequest {

    private String merchantId;
    private Integer amount;
    private String callbackUrl;
    private String description;
    private Metadata metadata;

    @Data
    public static class Metadata {
        private String mobile;
        private String email;
        private String orderId;


    }
}


