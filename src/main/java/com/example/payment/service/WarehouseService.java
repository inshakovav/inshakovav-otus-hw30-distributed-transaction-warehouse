package com.example.payment.service;

import com.example.payment.dto.PaymentExecutedMessage;
import com.example.payment.dto.ProductReservedMessage;
import com.example.payment.dto.WarehouseReservationRejectedMessage;
import com.example.payment.entity.ProductReservationEntity;
import com.example.payment.entity.ProductReservationStatus;
import com.example.payment.kafka.KafkaProducerService;
import com.example.payment.repository.ProductReservedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final ProductReservedRepository productReservedRepository;
    private final KafkaProducerService kafkaProducerService;
    private final SageCompensationService sageCompensationService;

    public void process(PaymentExecutedMessage message) {
        ProductReservationEntity productReservationEntity = saveToDb(message);
        boolean isPaymentSuccess = reserveProduct(message.getOrderId());
        sendReservationResult(message, productReservationEntity, isPaymentSuccess);
    }

    private void sendReservationResult(PaymentExecutedMessage message, ProductReservationEntity productReservationEntity, boolean isPaymentSuccess) {
        if (isPaymentSuccess) {
            log.info("Product successfully reserved: {}", message);
            ProductReservedMessage reservedMessage = ProductReservedMessage.builder()
                    .orderId(message.getOrderId())
                    .orderDescription(message.getOrderDescription())
                    .deliveryAddress(message.getDeliveryAddress())
                    .reservationId(productReservationEntity.getId())
                    .build();
            kafkaProducerService.sendProductReserved(reservedMessage);
        } else {
            log.warn("Product NOT reserved: {}", message);
            WarehouseReservationRejectedMessage rejectedMessage = WarehouseReservationRejectedMessage.builder()
                    .orderId(message.getOrderId())
                    .reservationId(productReservationEntity.getId())
                    .errorCode("Order id is 6 or 11 or 21 etc")
                    .build();
            kafkaProducerService.sendProductReservationRejected(rejectedMessage);
            sageCompensationService.executeWarehouseReject(rejectedMessage);
        }
    }

    public ProductReservationEntity saveToDb(PaymentExecutedMessage message) {
        ProductReservationEntity entity = new ProductReservationEntity();
        entity.setStatus(ProductReservationStatus.RESERVED);
        entity.setOrderId(message.getOrderId());
        ProductReservationEntity dbEntity = productReservedRepository.save(entity);
        return dbEntity;
    }

    /**
     *
     * @param orderId - from Order service
     * @return return false for all orderId divisible by 5.
     * Example: 5 - false, 10 - false, 2 - true
     */
    public boolean reserveProduct(Long orderId) {
        if (orderId < 5) {
            return true;
        }
        orderId += 4;
        return (orderId%5L) != 0;
    }
}
