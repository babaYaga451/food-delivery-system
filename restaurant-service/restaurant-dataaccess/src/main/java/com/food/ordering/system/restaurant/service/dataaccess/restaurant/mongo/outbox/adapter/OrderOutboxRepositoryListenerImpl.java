package com.food.ordering.system.restaurant.service.dataaccess.restaurant.mongo.outbox.adapter;


import static org.springframework.data.mongodb.core.query.Criteria.where;

import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.mongo.outbox.entity.OrderOutboxEntity;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.mongo.outbox.mapper.OrderOutboxDataAccessMapper;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.ports.input.outbox.listener.OrderOutboxRepositoryListener;
import com.mongodb.client.model.changestream.OperationType;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class OrderOutboxRepositoryListenerImpl implements OrderOutboxRepositoryListener {

  private final ReactiveMongoTemplate reactiveMongoTemplate;
  private final OrderOutboxDataAccessMapper orderOutboxDataAccessMapper;

  public OrderOutboxRepositoryListenerImpl(ReactiveMongoTemplate reactiveMongoTemplate,
      OrderOutboxDataAccessMapper orderOutboxDataAccessMapper) {
    this.reactiveMongoTemplate = reactiveMongoTemplate;
    this.orderOutboxDataAccessMapper = orderOutboxDataAccessMapper;
  }


  @Override
  public Flux<OrderOutboxMessage> getOrderOutboxMessageByOutboxStatus(OutboxStatus outboxStatus,
      String type) {
    return reactiveMongoTemplate.changeStream(OrderOutboxEntity.class)
        .watchCollection("order_outbox")
        .filter(where("outboxStatus").is(outboxStatus.name())
            .and("type").is(type))
        .listen()
        .filter(event -> event.getOperationType() == OperationType.INSERT)
        .mapNotNull(ChangeStreamEvent::getBody)
        .map(orderOutboxDataAccessMapper::orderOutboxEntityToOrderOutboxMessage);
  }
}
