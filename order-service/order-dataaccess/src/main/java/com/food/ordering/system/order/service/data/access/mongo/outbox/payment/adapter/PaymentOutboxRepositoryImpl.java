package com.food.ordering.system.order.service.data.access.mongo.outbox.payment.adapter;

import com.food.ordering.system.order.service.data.access.mongo.outbox.payment.exception.PaymentOutboxNotFoundException;
import com.food.ordering.system.order.service.data.access.mongo.outbox.payment.mapper.PaymentOutboxDataAccessMapper;
import com.food.ordering.system.order.service.data.access.mongo.outbox.payment.repository.PaymentOutboxMongoRepository;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.repository.PaymentOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PaymentOutboxRepositoryImpl implements PaymentOutboxRepository {

  private final PaymentOutboxMongoRepository paymentOutboxMongoRepository;
  private final PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper;

  public PaymentOutboxRepositoryImpl(PaymentOutboxMongoRepository paymentOutboxMongoRepository,
      PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper) {
    this.paymentOutboxMongoRepository = paymentOutboxMongoRepository;
    this.paymentOutboxDataAccessMapper = paymentOutboxDataAccessMapper;
  }


  @Override
  public OrderPaymentOutboxMessage save(OrderPaymentOutboxMessage orderPaymentOutboxMessage) {
    return paymentOutboxDataAccessMapper
        .paymentOutboxEntityToOrderPaymentOutboxMessage(paymentOutboxMongoRepository
            .save(paymentOutboxDataAccessMapper
                .orderPaymentOutboxMessageToPaymentOutboxEntity(orderPaymentOutboxMessage)));
  }

  @Override
  public Optional<List<OrderPaymentOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatue(
      String type, OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
    return Optional.of(paymentOutboxMongoRepository
        .findByTypeAndOutboxStatusAndSagaStatusIn(type,
            outboxStatus, Arrays.asList(sagaStatus))
        .orElseThrow(() -> new PaymentOutboxNotFoundException(
            "Payment outbox not found for saga type " + type))
        .stream()
        .map(paymentOutboxDataAccessMapper::paymentOutboxEntityToOrderPaymentOutboxMessage)
        .collect(Collectors.toList()));
  }

  @Override
  public Optional<OrderPaymentOutboxMessage> findByTypeAndSagaIdAndSagaStatus(String type,
      UUID sagaId, SagaStatus... sagaStatus) {
    return paymentOutboxMongoRepository
        .findByTypeAndSagaIdAndSagaStatusIn(type, sagaId, Arrays.asList(sagaStatus))
        .map(paymentOutboxDataAccessMapper::paymentOutboxEntityToOrderPaymentOutboxMessage);
  }

  @Override
  public void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus,
      SagaStatus... sagaStatus) {
    paymentOutboxMongoRepository
        .deleteByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, Arrays.asList(sagaStatus));
  }
}
