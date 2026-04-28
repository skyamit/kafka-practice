package com.amit_codes.Consumer.service;

import com.amit_codes.Consumer.entity.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.Acknowledgment;

@Service
public class OrderConsumer {

    @KafkaListener(topics = "order-topic", groupId = "order-group")
    public void consume(OrderEvent event, Acknowledgment ack) {
        System.out.println("Received Order: " + event.getOrderId());
        try {
            processOrder(event);
            ack.acknowledge();
        } catch (Exception e) {
            System.out.println("Error processing event" +  e.getMessage());
            // do NOT ack → Kafka will retry
        }
    }

    private void processOrder(OrderEvent event) {
        System.out.println("Processing payment for: " + event.getAmount());
    }
}