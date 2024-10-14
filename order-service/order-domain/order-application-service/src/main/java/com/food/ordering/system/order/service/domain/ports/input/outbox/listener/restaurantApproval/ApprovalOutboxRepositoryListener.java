package com.food.ordering.system.order.service.domain.ports.input.outbox.listener.restaurantApproval;

import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import reactor.core.publisher.Flux;

public interface ApprovalOutboxRepositoryListener {

  Flux<OrderApprovalOutboxMessage> watchRestaurantApprovalOutboxCollection(
      OutboxStatus outboxStatus,
      SagaStatus... sagaStatus);

}
