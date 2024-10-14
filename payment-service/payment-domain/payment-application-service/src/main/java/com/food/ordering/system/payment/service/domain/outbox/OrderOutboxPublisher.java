package com.food.ordering.system.payment.service.domain.outbox;

import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;

import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.food.ordering.system.payment.service.domain.ports.input.outbox.listener.OrderOutboxRepositoryListener;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class OrderOutboxPublisher {

  private final OrderOutboxHelper orderOutboxHelper;
  private final PaymentResponseMessagePublisher paymentResponseMessagePublisher;
  private final OrderOutboxRepositoryListener orderOutboxRepositoryListener;

  public OrderOutboxPublisher(OrderOutboxHelper orderOutboxHelper,
      PaymentResponseMessagePublisher paymentResponseMessagePublisher,
      OrderOutboxRepositoryListener orderOutboxRepositoryListener) {
    this.orderOutboxHelper = orderOutboxHelper;
    this.paymentResponseMessagePublisher = paymentResponseMessagePublisher;
    this.orderOutboxRepositoryListener = orderOutboxRepositoryListener;
  }

  @Transactional
  @PostConstruct
  public void publishOutBoxMessage() {
    Flux<OrderOutboxMessage> orderOutboxMessageFlux =
        orderOutboxRepositoryListener.getOrderOutboxMessageByOutboxStatus(OutboxStatus.STARTED,
            ORDER_SAGA_NAME);

    orderOutboxMessageFlux
        .doOnNext(orderOutboxMessage -> {
          log.info("Received Order Outbox message with id: {}, sending to kafka!",
              orderOutboxMessage.getId().toString());
          paymentResponseMessagePublisher.publish(orderOutboxMessage,
              orderOutboxHelper::updateOutboxMessage);
        })
        .subscribe();
  }
}
