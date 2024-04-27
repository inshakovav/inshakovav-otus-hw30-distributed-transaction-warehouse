package com.example.payment.service;

import com.example.payment.dto.DeliveryExecutedMessage;
import com.example.payment.dto.DeliveryRejectedMessage;
import com.example.payment.dto.WarehouseReservationRejectedMessage;
import com.example.payment.entity.ProductReservationEntity;
import com.example.payment.entity.ProductReservationStatus;
import com.example.payment.repository.ProductReservedRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SageCompensationService {

    private final ProductReservedRepository repository;

    @Transactional
    public void executeWarehouseReject(WarehouseReservationRejectedMessage message) {
        ProductReservationEntity payment = findReservationByOrderId(message.getOrderId());
        repository.deleteById(payment.getId());
        log.info("Delete reservation. Warehouse reservation was rejected: {}", message);
    }

    @Transactional
    public void executeDeliveryReject(DeliveryRejectedMessage message) {
        ProductReservationEntity payment = findReservationByOrderId(message.getOrderId());
        repository.deleteById(payment.getId());
        log.info("Delete reservation. Delivery was rejected: {}", message);
    }


    @Transactional
    public void executeDeliveryExecution(DeliveryExecutedMessage message) {
        ProductReservationEntity reservation = findReservationByOrderId(message.getOrderId());
        log.info("Delivery was succeeded. ---Finish---: {}", message);
        reservation.setStatus(ProductReservationStatus.DELIVERY_SUCCEEDED);
        repository.save(reservation);
    }

    private ProductReservationEntity findReservationByOrderId(long orderId) {
        return repository.findFirstByOrderId(orderId)
                .orElseThrow(() -> new NumberFormatException("Wrong product reservation rejection. Can't find product by order id" + orderId));
    }
}
