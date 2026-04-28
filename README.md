# Kafka with Spring Boot (Producer & Consumer)

This project demonstrates a complete setup of Apache Kafka with Spring Boot using:
- Producer Service
- Consumer Service
- Docker-based Kafka setup
- Event-driven architecture

---

## 1. Overview

Apache Kafka is a distributed event streaming platform used for building real-time data pipelines and asynchronous systems.

### Key Concepts

- **Topic**: Logical channel for messages (e.g., `order-topic`)
- **Partition**: Divides topic for scalability and parallelism
- **Producer**: Sends messages to Kafka
- **Consumer**: Reads messages from Kafka
- **Consumer Group**: Enables load balancing
- **Offset**: Message position in partition
- **Broker**: Kafka server

---

## 2. Project Structure


com.amit.kafka
├── producer
│ ├── controller
│ ├── service
│ └── config
├── consumer
│ ├── listener
│ └── config
└── common
└── event


---

## 3. Event Model

```java
package com.amit.kafka.common.event;

public class OrderEvent {
    private String orderId;
    private String userId;
    private double amount;

    // getters and setters
}
```

##4. Docker Setup
```
docker-compose.yml
version: '3'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

## Run Kafka
```
docker-compose up -d
```

## 5. Producer Configuration
application.yml
```
spring:
  kafka:
    bootstrap-servers: localhost:9092

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

      properties:
        spring.json.add.type.headers: false
        acks: all
        retries: 2147483647
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5
        delivery.timeout.ms: 120000
        linger.ms: 5
        batch.size: 16384
```

## Producer Service
```
@Service
public class OrderProducer {

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void sendOrder(OrderEvent event) {
        kafkaTemplate.send("order-topic", event.getOrderId(), event);
    }
}
```

## Controller
```
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderProducer producer;

    @PostMapping
    public String createOrder(@RequestBody OrderEvent event) {
        producer.sendOrder(event);
        return "Order sent to Kafka";
    }
}
```

## 6. Consumer Configuration
application.yml
```
spring:
  kafka:
    bootstrap-servers: localhost:9092

    consumer:
      group-id: order-group
      auto-offset-reset: earliest
      enable-auto-commit: false

      key-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer

      properties:
        spring.deserializer.key.delegate.class: org.apache.kafka.common.serialization.StringDeserializer
        spring.deserializer.value.delegate.class: org.springframework.kafka.support.serializer.JsonDeserializer
        spring.json.trusted.packages: "*"
        spring.json.value.default.type: com.amit.kafka.common.event.OrderEvent

    listener:
      ack-mode: manual
```

## Consumer Listener
```
@Service
public class OrderConsumer {

    @KafkaListener(topics = "order-topic", groupId = "order-group")
    public void consume(OrderEvent event, Acknowledgment ack) {

        try {
            System.out.println("Received order: " + event.getOrderId());

            process(event);

            ack.acknowledge();

        } catch (Exception e) {
            System.out.println("Error processing event");
        }
    }

    private void process(OrderEvent event) {
        // business logic
    }
}
```
## 7. Message Flow
Producer → Kafka Topic → Consumer → Processing → Offset Commit

## 8. Partitioning and Scaling
Partitions determine parallelism
Max consumers = number of partitions

Example:
3 partitions → max 3 active consumers
Key-based partitioning
kafkaTemplate.send("order-topic", orderId, event);
Same key → same partition
Maintains ordering

## 9. Offset Management
Wrong Approach
read → commit → fail → data lost
Correct Approach
read → process → commit

## 10. Failure Handling
Common Issues
1. ClassNotFoundException
Cause: Different package names in producer and consumer
Fix: Use shared common module
2. Deserialization Error
spring.json.trusted.packages: "*"
3. Old Messages Breaking Consumer
Change group-id
Or delete topic
4. Infinite Retry Loop
Use ErrorHandlingDeserializer


## 11. Delivery Guarantees

Type	Behavior
At-most-once	No duplicates, possible loss
At-least-once	No loss, possible duplicates
Exactly-once	No loss, no duplicates


## 12. Best Practices
Use key-based partitioning
Disable auto commit
Use manual acknowledgment
Enable idempotent producer
Use shared event models
Handle retries and failures
Monitor consumer lag


## 13. Important Concepts
Ordering is guaranteed only within a partition
Consumer groups enable load balancing
Partitions control scalability
Offsets track consumption progress


## 14. Run the Application
```
Step 1: Start Kafka
docker-compose up -d
Step 2: Start Consumer Service
Step 3: Start Producer Service
Step 4: Send Request
```

## POST http://localhost:8081/orders
```
{
  "orderId": "1",
  "userId": "1",
  "amount": 200
}
```

## 15. Summary
Kafka enables asynchronous communication between services
Producer sends events, consumer processes them
Reliability depends on:
  Producer configuration
  Consumer offset handling
  Error handling strategy
