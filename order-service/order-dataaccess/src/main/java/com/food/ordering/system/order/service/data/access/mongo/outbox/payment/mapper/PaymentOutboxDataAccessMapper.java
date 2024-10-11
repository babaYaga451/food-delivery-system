package com.food.ordering.system.order.service.data.access.mongo.outbox.payment.mapper;

import static com.food.ordering.system.domain.DomainConstants.UTC;

import com.food.ordering.system.order.service.data.access.mongo.outbox.payment.entity.PaymentOutboxEntity;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class PaymentOutboxDataAccessMapper {

  public PaymentOutboxEntity orderPaymentOutboxMessageToPaymentOutboxEntity(
      OrderPaymentOutboxMessage orderPaymentOutboxMessage) {
    return PaymentOutboxEntity.builder()
        .id(orderPaymentOutboxMessage.getId())
        .sagaId(orderPaymentOutboxMessage.getSagaId())
        .createdAt(orderPaymentOutboxMessage.getCreatedAt().toLocalDateTime())
        .type(orderPaymentOutboxMessage.getType())
        .payload(orderPaymentOutboxMessage.getPayload())
        .orderStatus(orderPaymentOutboxMessage.getOrderStatus())
        .sagaStatus(orderPaymentOutboxMessage.getSagaStatus())
        .outboxStatus(orderPaymentOutboxMessage.getOutboxStatus())
        .version(orderPaymentOutboxMessage.getVersion())
        .build();
  }

  public OrderPaymentOutboxMessage paymentOutboxEntityToOrderPaymentOutboxMessage(
      PaymentOutboxEntity paymentOutboxEntity) {
    return OrderPaymentOutboxMessage.builder()
        .id(paymentOutboxEntity.getId())
        .sagaId(paymentOutboxEntity.getSagaId())
        .createdAt(paymentOutboxEntity.getCreatedAt().atZone(ZoneId.of(UTC)))
        .type(paymentOutboxEntity.getType())
        .payload(paymentOutboxEntity.getPayload())
        .orderStatus(paymentOutboxEntity.getOrderStatus())
        .outboxStatus(paymentOutboxEntity.getOutboxStatus())
        .sagaStatus(paymentOutboxEntity.getSagaStatus())
        .version(paymentOutboxEntity.getVersion())
        .build();
  }

}
