package com.food.ordering.system.payment.service.domain.outbox.scheduler;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderOutboxScheduler implements OutboxScheduler {

  private final OrderOutboxHelper orderOutboxHelper;
  private final PaymentResponseMessagePublisher paymentResponseMessagePublisher;

  public OrderOutboxScheduler(OrderOutboxHelper orderOutboxHelper,
      PaymentResponseMessagePublisher paymentResponseMessagePublisher) {
    this.orderOutboxHelper = orderOutboxHelper;
    this.paymentResponseMessagePublisher = paymentResponseMessagePublisher;
  }

  @Override
  @Transactional
  @Scheduled(fixedDelayString = "${payment-service.outbox-scheduler-fixed-rate}",
      initialDelayString = "${payment-service.outbox-scheduler-initial-delay}")
  public void processOutBoxMessage() {
    Optional<List<OrderOutboxMessage>> orderOutboxMessageResponse = orderOutboxHelper
        .getOrderOutboxMessageByOutboxStatus(OutboxStatus.STARTED);
    if (orderOutboxMessageResponse.isPresent() && !orderOutboxMessageResponse.get().isEmpty()) {
      List<OrderOutboxMessage> orderOutboxMessages = orderOutboxMessageResponse.get();
      log.info("Received {} Order Outbox message with ids: {}, sending to kafka!",
          orderOutboxMessages.size(),
          orderOutboxMessages.stream()
              .map(OrderOutboxMessage::getId)
              .map(UUID::toString)
              .collect(Collectors.joining(",")));
      orderOutboxMessages.forEach(orderOutboxMessage -> paymentResponseMessagePublisher
          .publish(orderOutboxMessage, orderOutboxHelper::updateOutboxMessage));
      log.info("{} Order Outbox message sent to message bus!", orderOutboxMessages.size());
    }
  }
}
