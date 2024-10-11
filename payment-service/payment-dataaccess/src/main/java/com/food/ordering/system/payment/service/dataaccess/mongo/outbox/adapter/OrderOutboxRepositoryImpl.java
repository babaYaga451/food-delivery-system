package com.food.ordering.system.payment.service.dataaccess.mongo.outbox.adapter;

import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.dataaccess.mongo.outbox.exception.OrderOutboxNotFoundException;
import com.food.ordering.system.payment.service.dataaccess.mongo.outbox.mapper.OrderOutboxDataAccessMapper;
import com.food.ordering.system.payment.service.dataaccess.mongo.outbox.repository.OrderOutboxMongoRepository;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.ports.output.repository.OrderOutboxRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderOutboxRepositoryImpl implements OrderOutboxRepository {

  private final OrderOutboxMongoRepository orderOutboxMongoRepository;
  private final OrderOutboxDataAccessMapper orderOutboxDataAccessMapper;

  public OrderOutboxRepositoryImpl(OrderOutboxMongoRepository orderOutboxMongoRepository,
      OrderOutboxDataAccessMapper orderOutboxDataAccessMapper) {
    this.orderOutboxMongoRepository = orderOutboxMongoRepository;
    this.orderOutboxDataAccessMapper = orderOutboxDataAccessMapper;
  }

  @Override
  public OrderOutboxMessage save(OrderOutboxMessage orderPaymentOutboxMessage) {
    return orderOutboxDataAccessMapper
        .orderOutboxEntityToOrderOutboxMessage(orderOutboxMongoRepository
            .save(orderOutboxDataAccessMapper
                .orderOutboxMessageToOutboxEntity(orderPaymentOutboxMessage)));
  }

  @Override
  public Optional<List<OrderOutboxMessage>> findByTypeAndOutboxStatus(String sagaType, OutboxStatus outboxStatus) {
    return Optional.of(orderOutboxMongoRepository.findByTypeAndOutboxStatus(sagaType, outboxStatus)
        .orElseThrow(() -> new OrderOutboxNotFoundException("Approval outbox object " +
            "cannot be found for saga type " + sagaType))
        .stream()
        .map(orderOutboxDataAccessMapper::orderOutboxEntityToOrderOutboxMessage)
        .collect(Collectors.toList()));
  }

  @Override
  public Optional<OrderOutboxMessage> findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(String sagaType,
      UUID sagaId,
      PaymentStatus paymentStatus,
      OutboxStatus outboxStatus) {
    return orderOutboxMongoRepository.findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(sagaType, sagaId,
            paymentStatus, outboxStatus)
        .map(orderOutboxDataAccessMapper::orderOutboxEntityToOrderOutboxMessage);
  }

  @Override
  public void deleteByTypeAndOutboxStatus(String sagaType, OutboxStatus outboxStatus) {
    orderOutboxMongoRepository.deleteByTypeAndOutboxStatus(sagaType, outboxStatus);
  }
}