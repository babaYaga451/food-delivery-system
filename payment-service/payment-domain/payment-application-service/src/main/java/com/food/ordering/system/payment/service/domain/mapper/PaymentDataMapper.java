package com.food.ordering.system.payment.service.domain.mapper;

import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.valueObject.PaymentId;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class PaymentDataMapper {

  public Payment paymentRequestModelToPayment(PaymentRequest paymentRequest) {
    return Payment.Builder.builder()
        .orderId(new com.food.ordering.system.domain.valueObject.OrderId(UUID.fromString(
            paymentRequest.getOrderId())))
        .paymentId(new PaymentId(UUID.fromString(paymentRequest.getId())))
        .customerId(new CustomerId(UUID.fromString(paymentRequest.getCustomerId())))
        .price(new com.food.ordering.system.domain.valueObject.Money(paymentRequest.getPrice()))
        .build();
  }
}
