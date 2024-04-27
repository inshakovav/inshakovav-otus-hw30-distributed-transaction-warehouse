package com.example.payment.kafka;

import com.example.payment.dto.DeliveryExecutedMessage;
import com.example.payment.dto.PaymentExecutedMessage;
import com.example.payment.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final WarehouseService warehouseService;
    @KafkaListener(topics = "${warehouse.kafka.payment-succeeded-topic}", groupId = "${warehouse.kafka.message-group-name}")
    public void receivePaymentExecutedMessage(PaymentExecutedMessage message) {
        try {
            warehouseService.process(message);
        } catch (Exception e) {
            log.warn("Kafka unknown error Order processing: ", message);
        }
    }
}
