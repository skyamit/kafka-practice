package com.amit_codes.Producer.service;

import com.amit_codes.Producer.entity.OrderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderProducer {

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void sendOrder(OrderEvent event) {
        kafkaTemplate.send("order-topic", event.getOrderId(), event);
    }
}