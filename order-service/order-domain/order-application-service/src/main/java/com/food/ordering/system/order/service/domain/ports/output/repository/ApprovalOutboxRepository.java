package com.food.ordering.system.order.service.domain.ports.output.repository;

import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ApprovalOutboxRepository {
  default OrderApprovalOutboxMessage save(OrderApprovalOutboxMessage orderApprovalOutboxMessage){return null;}

  default Optional<List<OrderApprovalOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatue(String type,
      OutboxStatus outboxStatus,
      SagaStatus... sagaStatus){return Optional.empty();};

  default Optional<OrderApprovalOutboxMessage> findByTypeAndSagaIdAndSagaStatus(String type,
      UUID sagaId,
      SagaStatus... sagaStatus){return Optional.empty();};

  default void deleteByTypeAndOutboxStatusAndSagaStatus(String type,
      OutboxStatus outboxStatus,
      SagaStatus... sagaStatus){};

  default Flux<OrderApprovalOutboxMessage> watchRestaurantApprovalOutboxCollection(OutboxStatus outboxStatus,
      SagaStatus... sagaStatus){return Flux.empty();};
}
