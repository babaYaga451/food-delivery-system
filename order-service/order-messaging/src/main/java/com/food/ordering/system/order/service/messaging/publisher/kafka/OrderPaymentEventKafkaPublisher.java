package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfig;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.PaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderPaymentEventKafkaPublisher implements PaymentRequestMessagePublisher {

  private final OrderMessagingDataMapper orderMessagingDataMapper;
  private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
  private final OrderServiceConfig orderServiceConfig;
  private final KafkaMessageHelper kafkaMessageHelper;

  public OrderPaymentEventKafkaPublisher(OrderMessagingDataMapper orderMessagingDataMapper,
      KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer,
      OrderServiceConfig orderServiceConfig,
      KafkaMessageHelper kafkaMessageHelper) {
    this.orderMessagingDataMapper = orderMessagingDataMapper;
    this.kafkaProducer = kafkaProducer;
    this.orderServiceConfig = orderServiceConfig;
    this.kafkaMessageHelper = kafkaMessageHelper;
  }


  @Override
  public void publish(OrderPaymentOutboxMessage orderPaymentOutboxMessage,
      BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback) {

    OrderPaymentEventPayload orderPaymentEventPayload =
        kafkaMessageHelper.getOrderEventPayload(
            orderPaymentOutboxMessage.getPayload(),
            OrderPaymentEventPayload.class);
    String sagaId = orderPaymentOutboxMessage.getSagaId().toString();
    log.info("Received OrderPaymentOutboxMessage for orderId {} and sagaId {}",
        orderPaymentEventPayload.getOrderId(),
        sagaId);

    try {
      PaymentRequestAvroModel paymentRequestAvroModel = orderMessagingDataMapper
          .orderPaymentEventToPaymentRequestAvroModel(sagaId, orderPaymentEventPayload);
      kafkaProducer.send(
          orderServiceConfig.getPaymentRequestTopicName(),
          sagaId,
          paymentRequestAvroModel,
          kafkaMessageHelper.getKafkaCallBack(orderServiceConfig.getPaymentRequestTopicName(),
              paymentRequestAvroModel,
              orderPaymentOutboxMessage,
              outboxCallback,
              orderPaymentEventPayload.getOrderId(),
              "PaymentRequestAvroModel"));

      log.info("OrderPaymentEventPayload sent to kafka for orderId: {} and sagaId: {}",
          orderPaymentEventPayload.getOrderId(), sagaId);

    } catch (Exception e) {
      log.error("OrderPaymentEventPayload sent to kafka failed for orderId: {} sagaId: {}, error: {}",
          orderPaymentEventPayload.getOrderId(), sagaId, e.getMessage());
    }
  }
}
