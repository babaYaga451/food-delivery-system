package com.food.ordering.system.order.service.domain.ports.input.outbox.listener.payment;

import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import reactor.core.publisher.Flux;

public interface PaymentOutboxRepositoryListener {

  Flux<OrderPaymentOutboxMessage> watchPaymentOutboxCollection(
      OutboxStatus outboxStatus,
      SagaStatus... sagaStatus);

}
