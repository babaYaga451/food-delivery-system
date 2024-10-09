package com.food.ordering.system.order.service.domain.outbox.scheduler.approval;

import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantApproval.RestaurantApprovalRequestMessagePublisher;
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
public class RestaurantApprovalOutboxScheduler implements OutboxScheduler {

  private final ApprovalOutboxHelper approvalOutboxHelper;
  private final RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher;

  public RestaurantApprovalOutboxScheduler(ApprovalOutboxHelper approvalOutboxHelper,
      RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher) {
    this.approvalOutboxHelper = approvalOutboxHelper;
    this.restaurantApprovalRequestMessagePublisher = restaurantApprovalRequestMessagePublisher;
  }


  @Override
  @Transactional
  @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}",
      initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
  public void processOutBoxMessage() {
    Optional<List<OrderApprovalOutboxMessage>> outBoxMessagesResponse =
        approvalOutboxHelper.getApprovalOutboxMessageByOutBoxStatusAndSagaStatus(OutboxStatus.STARTED,
            SagaStatus.STARTED, SagaStatus.PROCESSING);
    if (outBoxMessagesResponse.isPresent() && !outBoxMessagesResponse.get().isEmpty()) {
      List<OrderApprovalOutboxMessage> outboxMessages = outBoxMessagesResponse.get();
      log.info("Received {} orderApprovalOutboxMessage with ids: {} sending to message bus!",
          outboxMessages.size(),
          outboxMessages.stream()
              .map(OrderApprovalOutboxMessage::getId)
              .map(UUID::toString)
              .collect(Collectors.joining(",")));
      outboxMessages.forEach(orderApprovalOutboxMessage ->
          restaurantApprovalRequestMessagePublisher.publish(orderApprovalOutboxMessage, this::updateOutBoxStatus));
      log.info("{} orderApprovalOutboxMessage sent to message bus!", outboxMessages.size());
    }
  }

  private void updateOutBoxStatus(OrderApprovalOutboxMessage orderApprovalOutboxMessage,
      OutboxStatus outboxStatus) {
    orderApprovalOutboxMessage.setOutboxStatus(outboxStatus);
    approvalOutboxHelper.save(orderApprovalOutboxMessage);
    log.info("orderApprovalOutboxMessage is updated with outbox status: {}", outboxStatus.name());
  }
}
