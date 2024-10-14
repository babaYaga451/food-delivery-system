package com.food.ordering.system.restaurant.service.domain.ports.input.outbox.listener;

import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import reactor.core.publisher.Flux;

public interface OrderOutboxRepositoryListener {

  Flux<OrderOutboxMessage> getOrderOutboxMessageByOutboxStatus(OutboxStatus outboxStatus,
      String type);

}
