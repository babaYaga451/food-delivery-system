package com.food.ordering.system.order.service.domain;

import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.order.service.data.access.mongo.outbox.payment.entity.PaymentOutboxEntity;
import com.food.ordering.system.order.service.data.access.mongo.outbox.payment.repository.PaymentOutboxMongoRepository;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@Slf4j
@SpringBootTest(classes = OrderServiceApplication.class)
@Sql(value = {"classpath:sql/OrderPaymentSagaTestSetUp.sql"})
@Sql(value = {"classpath:sql/OrderPaymentSagaTestCleanUp.sql"}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class OrderPaymentSagaTest {

  @Autowired
  private OrderPaymentSaga orderPaymentSaga;

  @Autowired
  private PaymentOutboxMongoRepository paymentOutboxMongoRepository;

  private final UUID SAGA_ID = UUID.fromString("15a497c1-0f4b-4eff-b9f4-c402c8c07afa");
  private final UUID ORDER_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb17");
  private final UUID CUSTOMER_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb41");
  private final UUID PAYMENT_ID = UUID.randomUUID();
  private final BigDecimal PRICE = BigDecimal.valueOf(100);

  @BeforeEach
  void setUp() {
    paymentOutboxMongoRepository.deleteAll();
    PaymentOutboxEntity entity = PaymentOutboxEntity.builder()
        .id(UUID.fromString("904808e-286f-449b-9b56-b63ba8351cf2"))
        .sagaId(SAGA_ID)
        .type(ORDER_SAGA_NAME)
        .createdAt(LocalDateTime.now())
        .outboxStatus(OutboxStatus.STARTED)
        .sagaStatus(SagaStatus.STARTED)
        .orderStatus(OrderStatus.PENDING)
        .version(0)
        .build();
    paymentOutboxMongoRepository.save(entity);
  }

  @Test
  void testDoublePayment() {
    orderPaymentSaga.process(getPaymentResponse());
    orderPaymentSaga.process(getPaymentResponse());
  }

  @Test
  void testDoublePaymentThreads() throws InterruptedException {
    var thread1 = new Thread(() -> orderPaymentSaga.process(getPaymentResponse()));
    var thread2 = new Thread(() -> orderPaymentSaga.process(getPaymentResponse()));
    thread1.start();
    thread2.start();
    thread1.join();
    thread2.join();

    assertPaymentOutbox();
  }

  @Test
  void testDoublePaymentWithLatch() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(2);
    var thread1 = new Thread(() -> {
      try {
        orderPaymentSaga.process(getPaymentResponse());
      } catch (OptimisticLockingFailureException e) {
        log.error("OptimisticLockingFailureException for thread-1");
      } finally {
        latch.countDown();
      }
    });
    var thread2 = new Thread(() -> {
      try {
        orderPaymentSaga.process(getPaymentResponse());
      } catch (OptimisticLockingFailureException e) {
        log.error("OptimisticLockingFailureException for thread-2");
      } finally {
        latch.countDown();
      }
    });
    thread1.start();
    thread2.start();
    latch.await();

    assertPaymentOutbox();
  }

  private void assertPaymentOutbox() {
    Optional<PaymentOutboxEntity> paymentOutboxMessage =
        paymentOutboxMongoRepository.findByTypeAndSagaIdAndSagaStatusIn(ORDER_SAGA_NAME,
            SAGA_ID,
            List.of(SagaStatus.PROCESSING));
    assertTrue(paymentOutboxMessage.isPresent());
  }

  private PaymentResponse getPaymentResponse() {
    return PaymentResponse.builder()
        .id(UUID.randomUUID().toString())
        .paymentId(PAYMENT_ID.toString())
        .customerId(CUSTOMER_ID.toString())
        .orderId(ORDER_ID.toString())
        .price(PRICE)
        .sagaId(SAGA_ID.toString())
        .paymentStatus(PaymentStatus.COMPLETED)
        .createdAt(Instant.now())
        .failureMessages(new ArrayList<>())
        .build();
  }

}
