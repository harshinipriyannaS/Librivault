package com.librivault.dto.payment;

public class PaymentIntentResponse {
    
    private String clientSecret;
    private String paymentIntentId;
    private Long paymentId;
    
    // Constructors
    public PaymentIntentResponse() {}
    
    public PaymentIntentResponse(String clientSecret, String paymentIntentId, Long paymentId) {
        this.clientSecret = clientSecret;
        this.paymentIntentId = paymentIntentId;
        this.paymentId = paymentId;
    }
    
    // Getters and Setters
    public String getClientSecret() {
        return clientSecret;
    }
    
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    
    public String getPaymentIntentId() {
        return paymentIntentId;
    }
    
    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }
    
    public Long getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
}