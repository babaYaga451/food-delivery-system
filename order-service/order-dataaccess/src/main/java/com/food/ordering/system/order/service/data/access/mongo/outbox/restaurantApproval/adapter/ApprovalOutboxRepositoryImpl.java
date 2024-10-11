package com.food.ordering.system.order.service.data.access.mongo.outbox.restaurantApproval.adapter;

import com.food.ordering.system.order.service.data.access.mongo.outbox.restaurantApproval.exception.RestaurantApprovalOutboxNotFoundException;
import com.food.ordering.system.order.service.data.access.mongo.outbox.restaurantApproval.mapper.ApprovalOutboxDataAccessMapper;
import com.food.ordering.system.order.service.data.access.mongo.outbox.restaurantApproval.repository.ApprovalOutboxMongoRepository;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.repository.ApprovalOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ApprovalOutboxRepositoryImpl implements ApprovalOutboxRepository {

  private final ApprovalOutboxMongoRepository approvalOutboxMongoRepository;
  private final ApprovalOutboxDataAccessMapper approvalOutboxDataAccessMapper;

  public ApprovalOutboxRepositoryImpl(ApprovalOutboxMongoRepository approvalOutboxMongoRepository,
      ApprovalOutboxDataAccessMapper approvalOutboxDataAccessMapper) {
    this.approvalOutboxMongoRepository = approvalOutboxMongoRepository;
    this.approvalOutboxDataAccessMapper = approvalOutboxDataAccessMapper;
  }

  @Override
  public OrderApprovalOutboxMessage save(OrderApprovalOutboxMessage orderApprovalOutboxMessage) {
    return approvalOutboxDataAccessMapper
        .orderApprovalEntityToOrderApprovalOutboxMessage(approvalOutboxMongoRepository
            .save(approvalOutboxDataAccessMapper
                .orderApprovalEntityToOrderApprovalOutboxMessage(orderApprovalOutboxMessage)));
  }

  @Override
  public Optional<List<OrderApprovalOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatue(
      String type, OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
    return Optional.of(approvalOutboxMongoRepository
        .findByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, Arrays.asList(sagaStatus))
        .orElseThrow(() -> new RestaurantApprovalOutboxNotFoundException("Approval outbox"
            + "not found for saga type " + type))
        .stream()
        .map(approvalOutboxDataAccessMapper::orderApprovalEntityToOrderApprovalOutboxMessage)
        .collect(Collectors.toList()));
  }

  @Override
  public Optional<OrderApprovalOutboxMessage> findByTypeAndSagaIdAndSagaStatus(String type,
      UUID sagaId, SagaStatus... sagaStatus) {
    return approvalOutboxMongoRepository
        .findByTypeAndSagaIdAndSagaStatusIn(type, sagaId, Arrays.asList(sagaStatus))
        .map(approvalOutboxDataAccessMapper::orderApprovalEntityToOrderApprovalOutboxMessage);
  }

  @Override
  public void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus,
      SagaStatus... sagaStatus) {
    approvalOutboxMongoRepository
        .deleteByTypeAndOutboxStatusAndSagaStatusIn(type,
            outboxStatus,
            Arrays.asList(sagaStatus));
  }
}
