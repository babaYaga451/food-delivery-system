package com.food.ordering.system.order.service.domain.outbox.payment;

import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.PaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.domain.ports.output.repository.PaymentOutboxRepository;
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
public class PaymentOutboxPublisher {

  private final PaymentOutboxRepository paymentOutboxRepository;
  private final PaymentRequestMessagePublisher paymentRequestMessagePublisher;
  private final PaymentOutboxHelper paymentOutboxHelper;

  public PaymentOutboxPublisher(PaymentOutboxHelper paymentOutboxHelper,
      @Qualifier("paymentOutboxReactiveMongoRepository") PaymentOutboxRepository paymentOutboxRepository,
      PaymentRequestMessagePublisher paymentRequestMessagePublisher) {
    this.paymentOutboxRepository = paymentOutboxRepository;
    this.paymentRequestMessagePublisher = paymentRequestMessagePublisher;
    this.paymentOutboxHelper = paymentOutboxHelper;
  }

  @PostConstruct
  @Transactional
  @Async
  public void processOutBoxMessage() {
    Flux<OrderPaymentOutboxMessage> orderPaymentOutboxMessageFlux =
        paymentOutboxRepository.watchPaymentOutboxCollection(
            OutboxStatus.STARTED,
            SagaStatus.STARTED,
            SagaStatus.COMPENSATING);

    orderPaymentOutboxMessageFlux
        .doOnNext(orderPaymentOutboxMessage -> {
          log.info("Received OrderPaymentOutboxMessage with id: {} sending to message bus!",
              orderPaymentOutboxMessage.getId().toString());
          paymentRequestMessagePublisher.publish(orderPaymentOutboxMessage, this::updateOutBoxStatus);
        })
        .subscribe();
  }

  private void updateOutBoxStatus(OrderPaymentOutboxMessage orderPaymentOutboxMessage,
      OutboxStatus outboxStatus) {
    orderPaymentOutboxMessage.setOutboxStatus(outboxStatus);
    paymentOutboxHelper.save(orderPaymentOutboxMessage);
    log.info("OrderPaymentOutboxMessage is updated with outbox status: {}", outboxStatus.name());
  }
}
