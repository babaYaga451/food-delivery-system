package com.food.ordering.system.order.service.data.access.outbox.restaurantApproval.mapper;

import com.food.ordering.system.order.service.data.access.outbox.restaurantApproval.entity.ApprovalOutboxEntity;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class ApprovalOutboxDataAccessMapper {

  public ApprovalOutboxEntity orderApprovalEntityToOrderApprovalOutboxMessage(
      OrderApprovalOutboxMessage orderApprovalOutboxMessage) {
    return ApprovalOutboxEntity.builder()
        .id(orderApprovalOutboxMessage.getId())
        .sagaId(orderApprovalOutboxMessage.getSagaId())
        .createdAt(orderApprovalOutboxMessage.getCreatedAt())
        .type(orderApprovalOutboxMessage.getType())
        .payload(orderApprovalOutboxMessage.getPayload())
        .orderStatus(orderApprovalOutboxMessage.getOrderStatus())
        .sagaStatus(orderApprovalOutboxMessage.getSagaStatus())
        .outboxStatus(orderApprovalOutboxMessage.getOutboxStatus())
        .version(orderApprovalOutboxMessage.getVersion())
        .build();
  }

  public OrderApprovalOutboxMessage orderApprovalEntityToOrderApprovalOutboxMessage(
      ApprovalOutboxEntity approvalOutboxEntity) {
    return OrderApprovalOutboxMessage.builder()
        .id(approvalOutboxEntity.getId())
        .sagaId(approvalOutboxEntity.getSagaId())
        .createdAt(approvalOutboxEntity.getCreatedAt())
        .type(approvalOutboxEntity.getType())
        .payload(approvalOutboxEntity.getPayload())
        .orderStatus(approvalOutboxEntity.getOrderStatus())
        .sagaStatus(approvalOutboxEntity.getSagaStatus())
        .outboxStatus(approvalOutboxEntity.getOutboxStatus())
        .version(approvalOutboxEntity.getVersion())
        .build();
  }

}
