package com.food.ordering.system.restaurant.service.dataaccess.restaurant.mongo.outbox.mapper;

import static com.food.ordering.system.domain.DomainConstants.UTC;

import com.food.ordering.system.restaurant.service.dataaccess.restaurant.mongo.outbox.entity.OrderOutboxEntity;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
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
                .approvalStatus(orderOutboxMessage.getApprovalStatus())
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
                .approvalStatus(paymentOutboxEntity.getApprovalStatus())
                .version(paymentOutboxEntity.getVersion())
                .build();
    }

}
