package com.food.ordering.system.order.service.domain.outbox.payment;

import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentOutboxCleanerScheduler implements OutboxScheduler {

  private final PaymentOutboxHelper paymentOutboxHelper;

  public PaymentOutboxCleanerScheduler(PaymentOutboxHelper paymentOutboxHelper) {
    this.paymentOutboxHelper = paymentOutboxHelper;
  }

  @Override
  @Scheduled(cron = "@midnight")
  public void processOutBoxMessage() {
    Optional<List<OrderPaymentOutboxMessage>> outboxMessagesResponse =
        paymentOutboxHelper.getPaymentOutboxMessageByOutBoxStatusAndSagaStatus(
            OutboxStatus.COMPLETED,
            SagaStatus.COMPENSATED,
            SagaStatus.FAILED,
            SagaStatus.SUCCEEDED);

    if (outboxMessagesResponse.isPresent() && !outboxMessagesResponse.get().isEmpty()) {
      List<OrderPaymentOutboxMessage> outboxMessages = outboxMessagesResponse.get();
      log.info("Received {} orderPaymentOutboxMessages for clean-up. Payloads: {}",
          outboxMessages.size(),
          outboxMessages.stream()
              .map(OrderPaymentOutboxMessage::getPayload)
              .collect(Collectors.joining("\n")));
      paymentOutboxHelper.deletePaymentOutboxMessageByOutBoxStatusAndSagaStatus(
          OutboxStatus.COMPLETED,
          SagaStatus.COMPENSATED,
          SagaStatus.FAILED,
          SagaStatus.SUCCEEDED);
      log.info("{} OrderPaymentOutboxMessages deleted!", outboxMessages.size());
    }
  }
}
