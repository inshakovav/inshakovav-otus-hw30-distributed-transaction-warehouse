package com.example.payment;

import com.example.payment.dto.OrderCreatedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaOrderProducer {
    @Value("${payment.kafka.order-created-topic}")
    private String orderCreatedTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrder(OrderCreatedMessage message) {
        kafkaTemplate.send(orderCreatedTopic, message);
    }
}
