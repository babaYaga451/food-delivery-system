package com.food.ordering.system.payment.service.domain.outbox.scheduler;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderOutboxCleanerScheduler implements OutboxScheduler {

  private final OrderOutboxHelper orderOutboxHelper;

  public OrderOutboxCleanerScheduler(OrderOutboxHelper orderOutboxHelper) {
    this.orderOutboxHelper = orderOutboxHelper;
  }

  @Scheduled(cron = "@midnight")
  @Override
  @Transactional
  public void processOutBoxMessage() {
    Optional<List<OrderOutboxMessage>> orderOutboxMessageResponse = orderOutboxHelper
        .getOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
    if (orderOutboxMessageResponse.isPresent() && !orderOutboxMessageResponse.get().isEmpty()) {
      List<OrderOutboxMessage> orderOutboxMessages = orderOutboxMessageResponse.get();
      log.info("Order Outbox Messages received for cleanup: {}", orderOutboxMessages.size());
      orderOutboxHelper.deleteOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
      log.info("Order Outbox Messages deleted: {}", orderOutboxMessages.size());
    }
  }
}
