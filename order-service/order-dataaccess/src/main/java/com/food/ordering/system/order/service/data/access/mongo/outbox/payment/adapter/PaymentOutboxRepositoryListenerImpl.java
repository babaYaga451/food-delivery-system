package com.food.ordering.system.order.service.data.access.mongo.outbox.payment.adapter;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import com.food.ordering.system.order.service.data.access.mongo.outbox.payment.entity.PaymentOutboxEntity;
import com.food.ordering.system.order.service.data.access.mongo.outbox.payment.mapper.PaymentOutboxDataAccessMapper;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.input.outbox.listener.payment.PaymentOutboxRepositoryListener;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.mongodb.client.model.changestream.OperationType;
import java.util.Arrays;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class PaymentOutboxRepositoryListenerImpl implements PaymentOutboxRepositoryListener {

  private final ReactiveMongoTemplate reactiveMongoTemplate;
  private final PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper;

  public PaymentOutboxRepositoryListenerImpl(ReactiveMongoTemplate reactiveMongoTemplate,
      PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper) {
    this.reactiveMongoTemplate = reactiveMongoTemplate;
    this.paymentOutboxDataAccessMapper = paymentOutboxDataAccessMapper;
  }

  @Override
  public Flux<OrderPaymentOutboxMessage> watchPaymentOutboxCollection(OutboxStatus outboxStatus,
      SagaStatus... sagaStatus) {
    return reactiveMongoTemplate
        .changeStream(PaymentOutboxEntity.class)
        .watchCollection("payment_outbox")
        .filter(
            where("outboxStatus").is(outboxStatus.name())
                .and("sagaStatus").in(Arrays.asList(sagaStatus)))
        .listen()
        .filter(event -> event.getOperationType() == OperationType.INSERT)
        .mapNotNull(ChangeStreamEvent::getBody)
        .map(paymentOutboxDataAccessMapper::paymentOutboxEntityToOrderPaymentOutboxMessage);
  }
}
