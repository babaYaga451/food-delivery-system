package com.food.ordering.system.order.service.data.access.outbox.restaurantApproval.adapter;

import com.food.ordering.system.order.service.data.access.outbox.restaurantApproval.exception.RestaurantApprovalOutboxNotFoundException;
import com.food.ordering.system.order.service.data.access.outbox.restaurantApproval.mapper.ApprovalOutboxDataAccessMapper;
import com.food.ordering.system.order.service.data.access.outbox.restaurantApproval.repository.ApprovalOutboxJpaRepository;
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

  private final ApprovalOutboxJpaRepository approvalOutboxJpaRepository;
  private final ApprovalOutboxDataAccessMapper approvalOutboxDataAccessMapper;

  public ApprovalOutboxRepositoryImpl(ApprovalOutboxJpaRepository approvalOutboxJpaRepository,
      ApprovalOutboxDataAccessMapper approvalOutboxDataAccessMapper) {
    this.approvalOutboxJpaRepository = approvalOutboxJpaRepository;
    this.approvalOutboxDataAccessMapper = approvalOutboxDataAccessMapper;
  }

  @Override
  public OrderApprovalOutboxMessage save(OrderApprovalOutboxMessage orderApprovalOutboxMessage) {
    return approvalOutboxDataAccessMapper
        .orderApprovalEntityToOrderApprovalOutboxMessage(approvalOutboxJpaRepository
            .save(approvalOutboxDataAccessMapper
                .orderApprovalEntityToOrderApprovalOutboxMessage(orderApprovalOutboxMessage)));
  }

  @Override
  public Optional<List<OrderApprovalOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatue(
      String type, OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
    return Optional.of(approvalOutboxJpaRepository
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
    return approvalOutboxJpaRepository
        .findByTypeAndSagaIdAndSagaStatusIn(type, sagaId, Arrays.asList(sagaStatus))
        .map(approvalOutboxDataAccessMapper::orderApprovalEntityToOrderApprovalOutboxMessage);
  }

  @Override
  public void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus,
      SagaStatus... sagaStatus) {
    approvalOutboxJpaRepository
        .deleteByTypeAndOutboxStatusAndSagaStatusIn(type,
            outboxStatus,
            Arrays.asList(sagaStatus));
  }
}
