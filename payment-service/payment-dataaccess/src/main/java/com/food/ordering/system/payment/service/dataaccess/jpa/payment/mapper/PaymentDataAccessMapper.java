package com.food.ordering.system.payment.service.dataaccess.jpa.payment.mapper;

import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.payment.service.dataaccess.jpa.payment.entity.PaymentEntity;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.valueObject.PaymentId;
import org.springframework.stereotype.Component;

@Component
public class PaymentDataAccessMapper {

  public PaymentEntity paymentToPaymentEntity(Payment payment) {
    return PaymentEntity
        .builder()
        .id(payment.getId().getValue())
        .customerId(payment.getCustomerId().getValue())
        .orderId(payment.getOrderId().getValue())
        .price(payment.getPrice().getAmount())
        .paymentStatus(payment.getPaymentStatus())
        .createdAt(payment.getCreatedAt())
        .build();
  }

  public Payment paymentEntityToPayment(PaymentEntity paymentEntity) {
    return Payment.Builder
        .builder()
        .paymentId(new PaymentId(paymentEntity.getId()))
        .customerId(new CustomerId(paymentEntity.getCustomerId()))
        .orderId(new OrderId(paymentEntity.getOrderId()))
        .price(new Money(paymentEntity.getPrice()))
        .paymentStatus(paymentEntity.getPaymentStatus())
        .createdAt(paymentEntity.getCreatedAt())
        .build();
  }

}
