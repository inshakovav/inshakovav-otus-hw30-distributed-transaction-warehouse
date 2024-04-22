package com.example.payment;

import com.example.payment.dto.PaymentExecutedMessage;
import com.example.payment.dto.ProductReservedMessage;
import com.example.payment.dto.WarehouseReservationRejectedMessage;
import com.example.payment.entity.ProductReservationEntity;
import com.example.payment.repository.ProductReservedRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@ActiveProfiles("test")
class WarehouseApplicationTests {

    @Autowired
    ProductReservedRepository productReservedRepository;

    @Autowired
    private KafkaSucceededConsumer kafkaSucceededConsumer;

    @Autowired
    private KafkaRejectedConsumer kafkaRejectedConsumer;

    @Autowired
    private KafkaOrderProducer kafkaOrderProducer;

    @Test
    void paymentSucceededTest() throws InterruptedException {
        // setup
        PaymentExecutedMessage paymentExecutedMessage = PaymentExecutedMessage.builder()
                .orderId(2L) // Reservation will be succeeded
                .orderDescription("Description of order")
                .productId(123L)
                .deliveryAddress("г. Москва, пер. Камергерский")
                .paymentId(12L)
                .build();

        // act
        kafkaOrderProducer.sendPaymentExecuted(paymentExecutedMessage);

        // verify
        boolean messageConsumed = kafkaSucceededConsumer.getLatch().await(10, TimeUnit.SECONDS);
        long lastProductId = getWarehouseEntity();
        assertTrue(messageConsumed);
        ProductReservedMessage executedMessage = kafkaSucceededConsumer.getExecutedMessage();
        assertEquals(executedMessage.getOrderId(), 2L);
        assertEquals(executedMessage.getOrderDescription(), "Description of order");
        assertEquals(executedMessage.getDeliveryAddress(), "г. Москва, пер. Камергерский");
        assertEquals(executedMessage.getReservationId(), lastProductId);
    }

    private long getWarehouseEntity() {
        Optional<ProductReservationEntity> lastProduct = productReservedRepository.findFirstByOrderByIdDesc();
        log.info("Last record={}", lastProduct);
        if (lastProduct.isEmpty()) {
            fail("DB error, Warehouse doesn't saved");
        }
        return lastProduct.get().getId();
    }

    @Test
    void paymentRejectedTest() throws InterruptedException {
        // setup
        PaymentExecutedMessage paymentExecutedMessage = PaymentExecutedMessage.builder()
                .orderId(6L) // Reservation will be rejected
                .orderDescription("Description of order")
                .productId(123L)
                .deliveryAddress("г. Москва, пер. Камергерский")
                .paymentId(12L)
                .build();

        // act
        kafkaOrderProducer.sendPaymentExecuted(paymentExecutedMessage);

        // verify
        boolean messageConsumed = kafkaRejectedConsumer.getLatch().await(10, TimeUnit.SECONDS);
        long lastProductId = getWarehouseEntity();
        assertTrue(messageConsumed);
        WarehouseReservationRejectedMessage message = kafkaRejectedConsumer.getRejectedMessage();
        assertEquals(message.getOrderId(), 6L);
        assertEquals(message.getReservationId(), lastProductId);
        assertEquals(message.getErrorCode(), "Order id is 6 or 11 or 21 etc");
    }
}
