package com.food.ordering.system.order.service.domain.outbox.scheduler.approval;

import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
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
public class RestaurantApprovalOutboxCleanerScheduler implements OutboxScheduler {

  private final ApprovalOutboxHelper approvalOutboxHelper;

  public RestaurantApprovalOutboxCleanerScheduler(ApprovalOutboxHelper approvalOutboxHelper) {
    this.approvalOutboxHelper = approvalOutboxHelper;
  }


  @Override
  @Scheduled(cron = "@midnight")
  public void processOutBoxMessage() {
    Optional<List<OrderApprovalOutboxMessage>> outboxMessagesResponse =
        approvalOutboxHelper.getApprovalOutboxMessageByOutBoxStatusAndSagaStatus(
            OutboxStatus.COMPLETED,
            SagaStatus.COMPENSATED,
            SagaStatus.FAILED,
            SagaStatus.SUCCEEDED);

    if (outboxMessagesResponse.isPresent() && !outboxMessagesResponse.get().isEmpty()) {
      List<OrderApprovalOutboxMessage> outboxMessages = outboxMessagesResponse.get();
      log.info("Received {} orderPaymentOutboxMessages for clean-up. Payloads: {}",
          outboxMessages.size(),
          outboxMessages.stream()
              .map(OrderApprovalOutboxMessage::getPayload)
              .collect(Collectors.joining("\n")));
      approvalOutboxHelper.deleteApprovalOutboxMessageByOutBoxStatusAndSagaStatus(
          OutboxStatus.COMPLETED,
          SagaStatus.COMPENSATED,
          SagaStatus.FAILED,
          SagaStatus.SUCCEEDED);
      log.info("{} OrderPaymentOutboxMessages deleted!", outboxMessages.size());
    }
  }
}
