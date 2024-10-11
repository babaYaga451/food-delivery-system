package com.food.ordering.system.order.service.domain.ports.output.repository;

import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface PaymentOutboxRepository {

  default OrderPaymentOutboxMessage save(OrderPaymentOutboxMessage orderPaymentOutboxMessage){
    return null;
  };

  default Optional<List<OrderPaymentOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatue(String type,
      OutboxStatus outboxStatus,
      SagaStatus... sagaStatus) {
    return Optional.empty();
  };

  default Optional<OrderPaymentOutboxMessage> findByTypeAndSagaIdAndSagaStatus(String type,
      UUID sagaId,
      SagaStatus... sagaStatus){
    return Optional.empty();
  };

  default void deleteByTypeAndOutboxStatusAndSagaStatus(String type,
      OutboxStatus outboxStatus,
      SagaStatus... sagaStatus){};

  default Flux<OrderPaymentOutboxMessage> watchPaymentOutboxCollection(
      OutboxStatus outboxStatus,
      SagaStatus... sagaStatus) {
    return Flux.empty();};
}
