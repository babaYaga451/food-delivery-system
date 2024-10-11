package com.food.ordering.system.order.service.domain.outbox.approval;

import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantApproval.RestaurantApprovalRequestMessagePublisher;
import com.food.ordering.system.order.service.domain.ports.output.repository.ApprovalOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class ApprovalOutboxPublisher {

  private final ApprovalOutboxRepository approvalOutboxRepository;
  private final RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher;
  private final ApprovalOutboxHelper approvalOutboxHelper;

  public ApprovalOutboxPublisher(
      @Qualifier("approvalOutboxReactiveMongoRepository") ApprovalOutboxRepository approvalOutboxRepository,
      RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher,
      ApprovalOutboxHelper approvalOutboxHelper) {
    this.approvalOutboxRepository = approvalOutboxRepository;
    this.restaurantApprovalRequestMessagePublisher = restaurantApprovalRequestMessagePublisher;
    this.approvalOutboxHelper = approvalOutboxHelper;
  }


  @PostConstruct
  @Transactional
  @Async
  public void processOutBoxMessage() {
    Flux<OrderApprovalOutboxMessage> approvalMessageFlux =
        approvalOutboxRepository.watchRestaurantApprovalOutboxCollection(
            OutboxStatus.STARTED,
            SagaStatus.STARTED,
            SagaStatus.PROCESSING);

    approvalMessageFlux
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
    log.info("orderApprovalOutboxMessage is updated with outbox status: {}", outboxStatus.name());
  }

}
