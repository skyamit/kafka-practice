package com.amit_codes.Consumer.entity;

import lombok.Data;

@Data
public class OrderEvent {
    private String orderId;
    private String userId;
    private double amount;

}