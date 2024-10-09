package com.food.ordering.system.payment.service.domain.outbox.scheduler;

import static com.food.ordering.system.domain.DomainConstants.UTC;
import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueObject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.exception.PaymentDomainException;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.ports.output.repository.OrderOutboxRepository;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderOutboxHelper {

  private final OrderOutboxRepository orderOutboxRepository;
  private final ObjectMapper objectMapper;

  public OrderOutboxHelper(OrderOutboxRepository orderOutboxRepository, ObjectMapper objectMapper) {
    this.orderOutboxRepository = orderOutboxRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  public Optional<OrderOutboxMessage> getCompletedOrderOutboxMessageBySagaIdAndPaymentStatus(
      UUID sagaId, PaymentStatus paymentStatus) {
    return orderOutboxRepository.findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(ORDER_SAGA_NAME,
        sagaId, paymentStatus, OutboxStatus.COMPLETED);
  }

  @Transactional(readOnly = true)
  public Optional<List<OrderOutboxMessage>> getOrderOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
    return orderOutboxRepository.findByTypeAndOutboxStatus(ORDER_SAGA_NAME, outboxStatus);
  }

  @Transactional
  public void deleteOrderOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
    orderOutboxRepository.deleteByTypeAndOutboxStatus(ORDER_SAGA_NAME, outboxStatus);
  }

  @Transactional
  public void saveOrderOutboxMessage(OrderEventPayload orderEventPayload,
      PaymentStatus paymentStatus,
      OutboxStatus outboxStatus,
      UUID sagaId) {
    save(OrderOutboxMessage.builder()
        .id(UUID.randomUUID())
        .sagaId(sagaId)
        .createdAt(orderEventPayload.getCreatedAt())
        .processedAt(ZonedDateTime.now(ZoneId.of(UTC)))
        .type(ORDER_SAGA_NAME)
        .payload(createPayload(orderEventPayload))
        .outboxStatus(outboxStatus)
        .paymentStatus(paymentStatus)
        .build());
  }

  private void save(OrderOutboxMessage orderOutboxMessage) {
    OrderOutboxMessage response = orderOutboxRepository.save(orderOutboxMessage);
    if (Objects.isNull(response)) {
      log.error("Failed to save order outbox message");
      throw new PaymentDomainException("Failed to save order outbox message");
    }
    log.info("Successfully saved order outbox message with id: {}", orderOutboxMessage.getId());
  }

  private String createPayload(OrderEventPayload orderEventPayload) {
    try {
      return objectMapper.writeValueAsString(orderEventPayload);
    } catch (JsonProcessingException e) {
      log.error("Could not serialize order event payload", e);
      throw new PaymentDomainException("Could not serialize order event payload", e);
    }
  }

  @Transactional
  public void updateOutboxMessage(OrderOutboxMessage orderOutboxMessage, OutboxStatus outboxStatus) {
    orderOutboxMessage.setOutboxStatus(outboxStatus);
    save(orderOutboxMessage);
    log.info("Order outbox table status updated as {}", outboxStatus.name());
  }
}
