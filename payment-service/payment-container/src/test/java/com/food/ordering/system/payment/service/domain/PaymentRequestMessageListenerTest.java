package com.food.ordering.system.payment.service.domain;

import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.food.ordering.system.domain.valueObject.PaymentOrderStatus;
import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.dataaccess.mongo.outbox.entity.OrderOutboxEntity;
import com.food.ordering.system.payment.service.dataaccess.mongo.outbox.repository.OrderOutboxMongoRepository;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;

@Slf4j
@SpringBootTest(classes = PaymentServiceApplication.class)
public class PaymentRequestMessageListenerTest {

  @Autowired
  private PaymentRequestMessageListener paymentRequestMessageListener;

  @Autowired
  private OrderOutboxMongoRepository orderOutboxMongoRepository;

  private final static String CUSTOMER_ID = "d215b5f8-0249-4dc5-89a3-51fd148cfb41";
  private final static BigDecimal PRICE = new BigDecimal("100");

  @Test
  void testDoublePayment() {
    String sagaId = UUID.randomUUID().toString();
    paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
    try {
      paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
    } catch (DuplicateKeyException e) {
      log.error("DuplicateKeyException occurred with mongo state: {}",
          (Objects.requireNonNull(e.getMessage())));
    }
    assertOrderOutbox(sagaId);
  }

  @Test
  void testDoublePaymentWithThreads() {
    String sagaId = UUID.randomUUID().toString();
    ExecutorService executor = null;

    try {
      executor = Executors.newFixedThreadPool(2);
      List<Callable<Object>> tasks = new ArrayList<>();

      tasks.add(Executors.callable(() -> {
        try {
          paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
        } catch (DuplicateKeyException e) {
          log.error("DuplicateKeyException occurred for thread 1 with error: {}", e.getMessage());
        }
      }));

      tasks.add(Executors.callable(() -> {
        try {
          paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
        } catch (DuplicateKeyException e) {
          log.error("DuplicateKeyException occurred for thread 2 with error: {}", e.getMessage());
        }
      }));

      executor.invokeAll(tasks);

      assertOrderOutbox(sagaId);
    } catch (InterruptedException e) {
      log.error("Error calling complete payment!", e);
    } finally {
      if (executor != null) {
        executor.shutdown();
      }
    }
  }

  private void assertOrderOutbox(String sagaId) {
    Optional<OrderOutboxEntity> orderOutboxEntity = orderOutboxMongoRepository
        .findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(ORDER_SAGA_NAME,
            UUID.fromString(sagaId),
            PaymentStatus.COMPLETED,
            OutboxStatus.STARTED);
    assertTrue(orderOutboxEntity.isPresent());
    assertEquals(orderOutboxEntity.get().getSagaId().toString(), sagaId);
  }

  private PaymentRequest getPaymentRequest(String sagaId) {
    return PaymentRequest.builder()
        .id(UUID.randomUUID().toString())
        .sagaId(sagaId)
        .orderId(UUID.randomUUID().toString())
        .paymentOrderStatus(PaymentOrderStatus.PENDING)
        .customerId(CUSTOMER_ID)
        .price(PRICE)
        .createdAt(Instant.now())
        .build();
  }

}