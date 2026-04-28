package com.amit_codes.Producer.controller;

import com.amit_codes.Producer.entity.OrderEvent;
import com.amit_codes.Producer.service.OrderProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderProducer producer;

    @PostMapping
    public String createOrder(@RequestBody OrderEvent event) {
        new Thread(() -> {
            long count = 0;
            for(int i = 0; i < 100; i++) {
                System.out.println(i);
                count++;
                event.setOrderId(count+"");
                producer.sendOrder(event);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (count == 1000) break;
            }
        }).start();
        return "Order sent to Kafka";
    }
}