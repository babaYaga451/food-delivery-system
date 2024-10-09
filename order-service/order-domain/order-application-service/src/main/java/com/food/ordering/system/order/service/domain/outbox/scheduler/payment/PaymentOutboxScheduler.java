package com.food.ordering.system.order.service.domain.outbox.scheduler.payment;

import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.PaymentRequestMessagePublisher;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
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
public class PaymentOutboxScheduler implements OutboxScheduler {

  private final PaymentOutboxHelper paymentOutboxHelper;
  private final PaymentRequestMessagePublisher paymentRequestMessagePublisher;

  public PaymentOutboxScheduler(PaymentOutboxHelper paymentOutboxHelper,
      PaymentRequestMessagePublisher paymentRequestMessagePublisher) {
    this.paymentOutboxHelper = paymentOutboxHelper;
    this.paymentRequestMessagePublisher = paymentRequestMessagePublisher;
  }


  @Override
  @Transactional
  @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}",
      initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
  public void processOutBoxMessage() {
    Optional<List<OrderPaymentOutboxMessage>> outBoxMessagesResponse =
        paymentOutboxHelper.getPaymentOutboxMessageByOutBoxStatusAndSagaStatus(OutboxStatus.STARTED,
            SagaStatus.STARTED, SagaStatus.COMPENSATING);
    if (outBoxMessagesResponse.isPresent() && !outBoxMessagesResponse.get().isEmpty()) {
      List<OrderPaymentOutboxMessage> outboxMessages = outBoxMessagesResponse.get();
      log.info("Received {} OrderPaymentOutboxMessage with ids: {} sending to message bus!",
          outboxMessages.size(),
          outboxMessages.stream()
              .map(OrderPaymentOutboxMessage::getId)
              .map(UUID::toString)
              .collect(Collectors.joining(",")));
      outboxMessages.forEach(orderPaymentOutboxMessage ->
          paymentRequestMessagePublisher.publish(orderPaymentOutboxMessage, this::updateOutBoxStatus));
      log.info("{} orderPaymentOutboxMessage sent to message bus!", outboxMessages.size());
    }
  }

  private void updateOutBoxStatus(OrderPaymentOutboxMessage orderPaymentOutboxMessage,
      OutboxStatus outboxStatus) {
    orderPaymentOutboxMessage.setOutboxStatus(outboxStatus);
    paymentOutboxHelper.save(orderPaymentOutboxMessage);
    log.info("OrderPaymentOutboxMessage is updated with outbox status: {}", outboxStatus.name());
  }
}
