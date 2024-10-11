package com.food.ordering.system.order.service.data.access.mongo.outbox.payment.adapter;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import com.food.ordering.system.order.service.data.access.mongo.outbox.payment.entity.PaymentOutboxEntity;
import com.food.ordering.system.order.service.data.access.mongo.outbox.payment.mapper.PaymentOutboxDataAccessMapper;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.repository.PaymentOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.mongodb.client.model.changestream.OperationType;
import java.util.Arrays;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class PaymentOutboxReactiveMongoRepository implements PaymentOutboxRepository {
  private final ReactiveMongoTemplate reactiveMongoTemplate;
  private final PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper;

  public PaymentOutboxReactiveMongoRepository(ReactiveMongoTemplate reactiveMongoTemplate,
      PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper) {
    this.reactiveMongoTemplate = reactiveMongoTemplate;
    this.paymentOutboxDataAccessMapper = paymentOutboxDataAccessMapper;
  }

  @Override
  public Flux<OrderPaymentOutboxMessage> watchPaymentOutboxCollection(OutboxStatus outboxStatus,
      SagaStatus... sagaStatus) {
    return reactiveMongoTemplate
        .changeStream(PaymentOutboxEntity.class)
        .filter(
            where("outboxStatus").is(outboxStatus.name())
                .and("sagaStatus").in(Arrays.asList(sagaStatus)))
        .listen()
        .filter(event -> event.getOperationType() == OperationType.INSERT)
        .mapNotNull(ChangeStreamEvent::getBody)
        .map(paymentOutboxDataAccessMapper::paymentOutboxEntityToOrderPaymentOutboxMessage);
  }
}
