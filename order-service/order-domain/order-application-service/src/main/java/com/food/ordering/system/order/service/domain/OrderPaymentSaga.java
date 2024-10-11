package com.food.ordering.system.order.service.domain;

import static com.food.ordering.system.domain.DomainConstants.UTC;

import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
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
public class OrderPaymentSaga implements SagaStep<PaymentResponse> {
  private final OrderSagaHelper orderSagaHelper;
  private final OrderDomainService orderDomainService;
  private final PaymentOutboxHelper paymentOutboxHelper;
  private final ApprovalOutboxHelper approvalOutboxHelper;
  private final OrderDataMapper orderDataMapper;

  public OrderPaymentSaga(
      OrderSagaHelper orderSagaHelper,
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
  public void process(PaymentResponse paymentResponse) {
    Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse = paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
        UUID.fromString(paymentResponse.getSagaId()),
        SagaStatus.STARTED);

    if (orderPaymentOutboxMessageResponse.isEmpty()) {
      log.info("An outbox message with saga id: {} is already processed!", paymentResponse.getSagaId());
      return;
    }

    OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();

    OrderPaidEvent domainEvent = completePayment(paymentResponse);

    SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(domainEvent.getOrder()
        .getOrderStatus());
    paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(orderPaymentOutboxMessage,
        domainEvent.getOrder().getOrderStatus(),
        sagaStatus));
    approvalOutboxHelper.saveApprovalOutboxMessage(
        orderDataMapper.orderPaidEventToOrderApprovalEventPayload(domainEvent),
        sagaStatus,
        domainEvent.getOrder().getOrderStatus(),
        OutboxStatus.STARTED,
        UUID.fromString(paymentResponse.getSagaId()));
    log.info("Order with id: {} is paid", domainEvent.getOrder().getId().getValue().toString());
  }

  @Override
  @Transactional
  public void rollback(PaymentResponse paymentResponse) {
    Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse =
        paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
            UUID.fromString(paymentResponse.getSagaId()),
            getCurrentSagaStatus(paymentResponse.getPaymentStatus()));

    if (orderPaymentOutboxMessageResponse.isEmpty()) {
      log.info("An outbox message with saga id: {} is already rolled back!", paymentResponse.getSagaId());
      return;
    }
    OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();

    Order order = rollbackPaymentforOrder(paymentResponse);

    SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());
    paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(orderPaymentOutboxMessage,
        order.getOrderStatus(),
        sagaStatus));

    if (paymentResponse.getPaymentStatus() == PaymentStatus.CANCELLED) {
      approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(paymentResponse.getSagaId(),
          order.getOrderStatus(), sagaStatus));
    }
    log.info("Order with id: {} is cancelled", order.getId().getValue().toString());
  }

  private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(
      OrderPaymentOutboxMessage orderPaymentOutboxMessage,
      OrderStatus orderStatus,
      SagaStatus sagaStatus) {
    orderPaymentOutboxMessage.setOrderStatus(orderStatus);
    orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
    orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
    return orderPaymentOutboxMessage;
  }

  private OrderPaidEvent completePayment(PaymentResponse paymentResponse) {
    log.info("Completing payment for order id: {}", paymentResponse.getOrderId());
    Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
    OrderPaidEvent domainEvent =
        orderDomainService.payOrder(order);
    orderSagaHelper.saveOrder(order);
    return domainEvent;
  }

  private SagaStatus[] getCurrentSagaStatus(PaymentStatus paymentStatus) {
    return switch (paymentStatus) {
      case COMPLETED -> new SagaStatus[] {SagaStatus.STARTED};
      case CANCELLED -> new SagaStatus[] {SagaStatus.PROCESSING};
      case FAILED -> new SagaStatus[] {SagaStatus.STARTED, SagaStatus.PROCESSING};
    };
  }

  private Order rollbackPaymentforOrder(PaymentResponse paymentResponse) {
    log.info("Cancelling order with id: {}", paymentResponse.getOrderId());
    Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
    orderDomainService.cancelOrder(order, paymentResponse.getFailureMessages());
    orderSagaHelper.saveOrder(order);
    return order;
  }

  private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(String sagaId,
      OrderStatus orderStatus,
      SagaStatus sagaStatus) {
    Optional<OrderApprovalOutboxMessage> approvalOutboxMessageResponse =
        approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(sagaId),
            SagaStatus.COMPENSATING);
    if (approvalOutboxMessageResponse.isEmpty()) {
      throw new OrderDomainException("Approval outbox message couldn't be found in " +
          SagaStatus.COMPENSATING.name() +
          " status!");
    }
    OrderApprovalOutboxMessage orderApprovalOutboxMessage = approvalOutboxMessageResponse.get();
    orderApprovalOutboxMessage.setOrderStatus(orderStatus);
    orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
    orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
    return orderApprovalOutboxMessage;
  }
}
