package com.librivault.dto.payment;

import java.math.BigDecimal;

import com.librivault.entity.enums.PaymentType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreatePaymentIntentRequest {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Payment type is required")
    private PaymentType type;
    
    private String description;
    
    private Long subscriptionId;
    
    private Long fineId;
    
    // Constructors
    public CreatePaymentIntentRequest() {}
    
    public CreatePaymentIntentRequest(BigDecimal amount, PaymentType type, String description) {
        this.amount = amount;
        this.type = type;
        this.description = description;
    }
    
    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public PaymentType getType() {
        return type;
    }
    
    public void setType(PaymentType type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getSubscriptionId() {
        return subscriptionId;
    }
    
    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
    
    public Long getFineId() {
        return fineId;
    }
    
    public void setFineId(Long fineId) {
        this.fineId = fineId;
    }
}