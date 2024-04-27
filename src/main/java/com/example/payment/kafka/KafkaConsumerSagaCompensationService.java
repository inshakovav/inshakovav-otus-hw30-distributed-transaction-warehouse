package com.example.payment.kafka;

import com.example.payment.dto.DeliveryExecutedMessage;
import com.example.payment.dto.DeliveryRejectedMessage;
import com.example.payment.service.SageCompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerSagaCompensationService {

    private final SageCompensationService sageCompensationService;

    @KafkaListener(topics = "${warehouse.kafka.delivery-rejected-topic}", groupId = "${warehouse.kafka.message-group-name}")
    public void receiveDeliveryRejected(DeliveryRejectedMessage message) {
        try {
            sageCompensationService.executeDeliveryReject(message);
        } catch (Exception e) {
            log.warn("Kafka unknown error Order processing: ", message);
        }
    }


    @KafkaListener(topics = "${warehouse.kafka.delivery-executed-topic}", groupId = "${warehouse.kafka.message-group-name}")
    public void receiveDeliveryExecuted(DeliveryExecutedMessage message) {
        try {
            sageCompensationService.executeDeliveryExecution(message);
        } catch (Exception e) {
            log.warn("Kafka unknown error Order processing: ", message);
        }
    }
}
