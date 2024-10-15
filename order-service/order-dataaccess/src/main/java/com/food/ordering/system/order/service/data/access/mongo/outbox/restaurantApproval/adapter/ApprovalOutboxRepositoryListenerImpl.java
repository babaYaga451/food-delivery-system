package com.food.ordering.system.order.service.data.access.mongo.outbox.restaurantApproval.adapter;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import com.food.ordering.system.order.service.data.access.mongo.outbox.restaurantApproval.entity.ApprovalOutboxEntity;
import com.food.ordering.system.order.service.data.access.mongo.outbox.restaurantApproval.mapper.ApprovalOutboxDataAccessMapper;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.input.outbox.listener.restaurantApproval.ApprovalOutboxRepositoryListener;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.mongodb.client.model.changestream.OperationType;
import java.util.Arrays;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ApprovalOutboxRepositoryListenerImpl implements ApprovalOutboxRepositoryListener {

  private final ReactiveMongoTemplate reactiveMongoTemplate;
  private final ApprovalOutboxDataAccessMapper approvalOutboxDataAccessMapper;

  public ApprovalOutboxRepositoryListenerImpl(ReactiveMongoTemplate reactiveMongoTemplate,
      ApprovalOutboxDataAccessMapper approvalOutboxDataAccessMapper) {
    this.reactiveMongoTemplate = reactiveMongoTemplate;
    this.approvalOutboxDataAccessMapper = approvalOutboxDataAccessMapper;
  }

  @Override
  public Flux<OrderApprovalOutboxMessage> watchRestaurantApprovalOutboxCollection(
      OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
    return reactiveMongoTemplate
        .changeStream(ApprovalOutboxEntity.class)
        .watchCollection("order_restaurant_approval_outbox")
        .filter(
            where("outboxStatus").is(outboxStatus.name())
                .and("sagaStatus").in(Arrays.asList(sagaStatus)))
        .listen()
        .filter(event -> event.getOperationType() == OperationType.INSERT)
        .mapNotNull(ChangeStreamEvent::getBody)
        .map(approvalOutboxDataAccessMapper::orderApprovalEntityToOrderApprovalOutboxMessage);
  }
}
