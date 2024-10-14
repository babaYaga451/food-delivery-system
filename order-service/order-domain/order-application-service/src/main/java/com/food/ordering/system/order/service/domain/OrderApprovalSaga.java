package com.food.ordering.system.order.service.domain;

import static com.food.ordering.system.domain.DomainConstants.UTC;

import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.approval.ApprovalOutboxHelper;
import com.food.ordering.system.order.service.domain.outbox.payment.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.SagaStep;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse> {

  private final OrderSagaHelper orderSagaHelper;
  private final OrderDomainService orderDomainService;
  private final PaymentOutboxHelper paymentOutboxHelper;
  private final ApprovalOutboxHelper approvalOutboxHelper;
  private final OrderDataMapper orderDataMapper;

  public OrderApprovalSaga(OrderSagaHelper orderSagaHelper,
      OrderDomainService orderDomainService,
      PaymentOutboxHelper paymentOutboxHelper,
      ApprovalOutboxHelper approvalOutboxHelper,
      OrderDataMapper orderDataMapper) {
    this.orderSagaHelper = orderSagaHelper;
    this.orderDomainService = orderDomainService;
    this.paymentOutboxHelper = paymentOutboxHelper;
    this.approvalOutboxHelper = approvalOutboxHelper;
    this.orderDataMapper = orderDataMapper;
  }


  @Override
  @Transactional
  public void process(RestaurantApprovalResponse restaurantApprovalResponse) {

    Optional<OrderApprovalOutboxMessage> approvalOutboxMessageResponse =
        approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
            UUID.fromString(restaurantApprovalResponse.getSagaId()),
            SagaStatus.PROCESSING);

    if (approvalOutboxMessageResponse.isEmpty()) {
      log.info("An outbox message with saga id: {} is already processed!",
          restaurantApprovalResponse.getSagaId());
      return;
    }

    OrderApprovalOutboxMessage orderApprovalOutboxMessage = approvalOutboxMessageResponse.get();
    Order order = approveOrder(restaurantApprovalResponse);

    SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());
    approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(orderApprovalOutboxMessage,
        sagaStatus,
        order.getOrderStatus()));
    paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(restaurantApprovalResponse.getSagaId(),
        order.getOrderStatus(),
        sagaStatus));

    log.info("Order with id: {} is approved", order.getId().getValue().toString());
  }

  private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(
      OrderApprovalOutboxMessage orderApprovalOutboxMessage,
      SagaStatus sagaStatus,
      OrderStatus orderStatus) {
    orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
    orderApprovalOutboxMessage.setOrderStatus(orderStatus);
    orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
    return orderApprovalOutboxMessage;
  }

  @Override
  @Transactional
  public void rollback(RestaurantApprovalResponse restaurantApprovalResponse) {
    Optional<OrderApprovalOutboxMessage> approvalOutboxMessageResponse =
        approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
            UUID.fromString(restaurantApprovalResponse.getSagaId()),
            SagaStatus.PROCESSING);

    if (approvalOutboxMessageResponse.isEmpty()) {
      log.info("An outbox message with saga id: {} is already rolled back!",
          restaurantApprovalResponse.getSagaId());
      return;
    }
    OrderApprovalOutboxMessage orderApprovalOutboxMessage = approvalOutboxMessageResponse.get();
    OrderCancelledEvent domainEvent = rollbackOrder(restaurantApprovalResponse);

    SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(domainEvent.getOrder().getOrderStatus());
    approvalOutboxHelper.save(
        getUpdatedApprovalOutboxMessage(orderApprovalOutboxMessage,
            sagaStatus,
            domainEvent.getOrder().getOrderStatus()));
    paymentOutboxHelper.savePaymentOutboxMessage(
        orderDataMapper.orderCancelledEventToOrderPaymentEventPayload(domainEvent),
        domainEvent.getOrder().getOrderStatus(),
        sagaStatus, OutboxStatus.STARTED,
        UUID.fromString(restaurantApprovalResponse.getSagaId()));

    log.info("Order with id: {} is cancelled", domainEvent.getOrder().getId().getValue().toString());
  }

  private Order approveOrder(RestaurantApprovalResponse restaurantApprovalResponse) {
    log.info("Approving order with id: {}", restaurantApprovalResponse.getOrderId());
    Order order = orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
    orderDomainService.approveOrder(order);
    orderSagaHelper.saveOrder(order);
    return order;
  }

  private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(String sagaId,
      OrderStatus orderStatus,
      SagaStatus sagaStatus) {
    Optional<OrderPaymentOutboxMessage> paymentOutboxMessageResponse =
        paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
            UUID.fromString(sagaId),
            SagaStatus.PROCESSING);
    if (paymentOutboxMessageResponse.isEmpty()) {
      throw new OrderDomainException("Payment outbox message not found in " +
          SagaStatus.PROCESSING.name() +
          " status!");
    }
    OrderPaymentOutboxMessage orderPaymentOutboxMessage = paymentOutboxMessageResponse.get();
    orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
    orderPaymentOutboxMessage.setOrderStatus(orderStatus);
    orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
    return orderPaymentOutboxMessage;
  }

  private OrderCancelledEvent rollbackOrder(RestaurantApprovalResponse restaurantApprovalResponse) {
    log.info("Cancelling order with id: {}", restaurantApprovalResponse.getOrderId());
    Order order = orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
    OrderCancelledEvent domainEvent = orderDomainService.
        cancelOrderPayment(order, restaurantApprovalResponse.getFailureMessages());
    orderSagaHelper.saveOrder(order);
    return domainEvent;
  }
}
