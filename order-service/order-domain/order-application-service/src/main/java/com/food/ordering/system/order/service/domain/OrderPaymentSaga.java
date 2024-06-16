package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.event.EmptyEvent;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantApproval.OrderPaidRestaurantRequestMessagePublisher;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.saga.SagaStep;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderPaymentSaga implements SagaStep<PaymentResponse, OrderPaidEvent, EmptyEvent> {
  private final OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher;
  private final OrderSagaHelper orderSagaHelper;
  private final OrderDomainService orderDomainService;

  public OrderPaymentSaga(
      OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher,
      OrderSagaHelper orderSagaHelper,
      OrderDomainService orderDomainService) {
    this.orderPaidRestaurantRequestMessagePublisher = orderPaidRestaurantRequestMessagePublisher;
    this.orderSagaHelper = orderSagaHelper;
    this.orderDomainService = orderDomainService;
  }

  @Override
  @Transactional
  public OrderPaidEvent process(PaymentResponse paymentResponse) {
    log.info("Completing payment for order id: {}", paymentResponse.getOrderId());
    Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
    OrderPaidEvent domainEvent =
        orderDomainService.payOrder(order, orderPaidRestaurantRequestMessagePublisher);
    orderSagaHelper.saveOrder(order);
    log.info("Order with id: {} is paid", order.getId().getValue().toString());
    return domainEvent;
  }

  @Override
  @Transactional
  public EmptyEvent rollback(PaymentResponse paymentResponse) {
    log.info("Cancelling order with id: {}", paymentResponse.getOrderId());
    Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
    orderDomainService.cancelOrder(order, paymentResponse.getFailureMessages());
    orderSagaHelper.saveOrder(order);
    log.info("Order with id: {} is cancelled", order.getId().getValue().toString());
    return EmptyEvent.INSTANCE;
  }
}
