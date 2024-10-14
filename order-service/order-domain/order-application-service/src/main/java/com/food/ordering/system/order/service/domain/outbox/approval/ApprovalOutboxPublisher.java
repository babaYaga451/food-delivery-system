package com.food.ordering.system.order.service.domain.outbox.approval;

import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.input.outbox.listener.restaurantApproval.ApprovalOutboxRepositoryListener;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantApproval.RestaurantApprovalRequestMessagePublisher;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class ApprovalOutboxPublisher {

  private final ApprovalOutboxRepositoryListener approvalOutboxRepositoryListener;
  private final RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher;
  private final ApprovalOutboxHelper approvalOutboxHelper;

  public ApprovalOutboxPublisher(ApprovalOutboxRepositoryListener approvalOutboxRepositoryListener,
      RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher,
      ApprovalOutboxHelper approvalOutboxHelper) {
    this.approvalOutboxRepositoryListener = approvalOutboxRepositoryListener;
    this.restaurantApprovalRequestMessagePublisher = restaurantApprovalRequestMessagePublisher;
    this.approvalOutboxHelper = approvalOutboxHelper;
  }

  @PostConstruct
  @Transactional
  public void publishOutboxMessage() {
    Flux<OrderApprovalOutboxMessage> orderApprovalOutboxMessageFlux =
        approvalOutboxRepositoryListener.watchRestaurantApprovalOutboxCollection(
            OutboxStatus.STARTED,
            SagaStatus.STARTED,
            SagaStatus.PROCESSING);

    orderApprovalOutboxMessageFlux
        .doOnNext(orderApprovalOutboxMessage -> {
          log.info("Received OrderApprovalOutboxMessage with id: {} sending to message bus!",
              orderApprovalOutboxMessage.getId().toString());
          restaurantApprovalRequestMessagePublisher.publish(orderApprovalOutboxMessage, this::updateOutBoxStatus);
        })
        .subscribe();
  }

  private void updateOutBoxStatus(OrderApprovalOutboxMessage orderApprovalOutboxMessage,
      OutboxStatus outboxStatus) {
    orderApprovalOutboxMessage.setOutboxStatus(outboxStatus);
    approvalOutboxHelper.save(orderApprovalOutboxMessage);
    log.info("OrderApprovalOutboxMessage is updated with outbox status: {}", outboxStatus.name());
  }
}
