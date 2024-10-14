package com.food.ordering.system.order.service.domain.outbox.scheduler.payment;

import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.repository.PaymentOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class PaymentOutboxHelper {

  private final PaymentOutboxRepository paymentOutboxRepository;
  private final ObjectMapper objectMapper;

  public PaymentOutboxHelper(PaymentOutboxRepository paymentOutboxRepository,
      ObjectMapper objectMapper) {
    this.paymentOutboxRepository = paymentOutboxRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  public Optional<List<OrderPaymentOutboxMessage>> getPaymentOutboxMessageByOutBoxStatusAndSagaStatus(
      OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
    return paymentOutboxRepository.findByTypeAndOutboxStatusAndSagaStatue(
        ORDER_SAGA_NAME,
        outboxStatus,
        sagaStatus);
  }

  @Transactional(readOnly = true)
  public Optional<OrderPaymentOutboxMessage> getPaymentOutboxMessageBySagaIdAndSagaStatus(UUID sagaId,
      SagaStatus... sagaStatus) {
    return paymentOutboxRepository.findByTypeAndSagaIdAndSagaStatus(
        ORDER_SAGA_NAME,
        sagaId,
        sagaStatus);
  }

  @Transactional
  public void save(OrderPaymentOutboxMessage orderPaymentOutboxMessage) {
    OrderPaymentOutboxMessage response = paymentOutboxRepository.save(orderPaymentOutboxMessage);
    if (Objects.isNull(response)) {
      log.info("Error saving orderPaymentOutboxMessage with outbox id: {}",
          orderPaymentOutboxMessage.getId());
      throw new OrderDomainException("Error saving orderPaymentOutboxMessage with outbox id: "
          + orderPaymentOutboxMessage.getId());
    }
    log.info("OrderPaymentOutboxMessage saved with outbox id: {}", orderPaymentOutboxMessage.getId());
  }

  @Transactional
  public void deletePaymentOutboxMessageByOutBoxStatusAndSagaStatus(OutboxStatus outboxStatus,
      SagaStatus... sagaStatus) {
    paymentOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatus(
        ORDER_SAGA_NAME,
        outboxStatus,
        sagaStatus);
  }

  @Transactional
  public void savePaymentOutboxMessage(OrderPaymentEventPayload paymentEventPayload,
      OrderStatus orderStatus,
      SagaStatus sagaStatus,
      OutboxStatus outboxStatus,
      UUID sagaId) {
    save(OrderPaymentOutboxMessage.builder()
        .id(UUID.randomUUID())
        .sagaId(sagaId)
        .createdAt(paymentEventPayload.getCreatedAt())
        .type(ORDER_SAGA_NAME)
        .payload(createPayload(paymentEventPayload))
        .sagaStatus(sagaStatus)
        .orderStatus(orderStatus)
        .outboxStatus(outboxStatus)
        .build());
  }

  private String createPayload(OrderPaymentEventPayload paymentEventPayload) {
    try {
      return objectMapper.writeValueAsString(paymentEventPayload);
    } catch (JsonProcessingException e) {
      log.error("Couldn't create OrderPaymentEventPayload object for id: {}",
          paymentEventPayload.getOrderId(), e);
      throw new OrderDomainException("Couldn't create OrderPaymentEventPayload object for id: "+
          paymentEventPayload.getOrderId(), e);
    }
  }
}
