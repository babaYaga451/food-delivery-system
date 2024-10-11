package com.food.ordering.system.payment.service.dataaccess.mongo.outbox.mapper;

import static com.food.ordering.system.domain.DomainConstants.UTC;

import com.food.ordering.system.payment.service.dataaccess.mongo.outbox.entity.OrderOutboxEntity;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class OrderOutboxDataAccessMapper {

  public OrderOutboxEntity orderOutboxMessageToOutboxEntity(OrderOutboxMessage orderOutboxMessage) {
    return OrderOutboxEntity.builder()
        .id(orderOutboxMessage.getId())
        .sagaId(orderOutboxMessage.getSagaId())
        .createdAt(orderOutboxMessage.getCreatedAt().toLocalDateTime())
        .type(orderOutboxMessage.getType())
        .payload(orderOutboxMessage.getPayload())
        .outboxStatus(orderOutboxMessage.getOutboxStatus())
        .paymentStatus(orderOutboxMessage.getPaymentStatus())
        .version(orderOutboxMessage.getVersion())
        .build();
  }

  public OrderOutboxMessage orderOutboxEntityToOrderOutboxMessage(OrderOutboxEntity paymentOutboxEntity) {
    return OrderOutboxMessage.builder()
        .id(paymentOutboxEntity.getId())
        .sagaId(paymentOutboxEntity.getSagaId())
        .createdAt(paymentOutboxEntity.getCreatedAt().atZone(ZoneId.of(UTC)))
        .type(paymentOutboxEntity.getType())
        .payload(paymentOutboxEntity.getPayload())
        .outboxStatus(paymentOutboxEntity.getOutboxStatus())
        .paymentStatus(paymentOutboxEntity.getPaymentStatus())
        .version(paymentOutboxEntity.getVersion())
        .build();
  }

}