package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {

  private final OrderPaymentSaga orderPaymentSaga;

  public PaymentResponseMessageListenerImpl(OrderPaymentSaga orderPaymentSaga) {
    this.orderPaymentSaga = orderPaymentSaga;
  }

  @Override
  public void paymentCompleted(PaymentResponse paymentResponse) {
    OrderPaidEvent domainEvent = orderPaymentSaga.process(paymentResponse);
    log.info("Publishing orderPaidEvent for order id: {}", paymentResponse.getOrderId());
    domainEvent.fire();
  }

  @Override
  public void paymentCancelled(PaymentResponse paymentResponse) {
    orderPaymentSaga.rollback(paymentResponse);
    log.info("Order is roll backed for orderId: {} with failureMessages: {}",
        paymentResponse.getOrderId(),
        String.join(",", paymentResponse.getFailureMessages()));
  }
}
