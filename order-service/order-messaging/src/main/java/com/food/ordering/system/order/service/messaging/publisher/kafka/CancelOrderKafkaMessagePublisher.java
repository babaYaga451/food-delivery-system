package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfig;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CancelOrderKafkaMessagePublisher implements
    OrderCancelledPaymentRequestMessagePublisher {

  private final OrderMessagingDataMapper orderMessagingDataMapper;
  private final OrderServiceConfig orderServiceConfig;
  private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
  private final OrderKafkaMessageHelper orderKafkaMessageHelper;

  public CancelOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper,
      OrderServiceConfig orderServiceConfig,
      KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer,
      OrderKafkaMessageHelper orderKafkaMessageHelper) {
    this.orderMessagingDataMapper = orderMessagingDataMapper;
    this.orderServiceConfig = orderServiceConfig;
    this.kafkaProducer = kafkaProducer;
    this.orderKafkaMessageHelper = orderKafkaMessageHelper;
  }

  @Override
  public void publish(OrderCancelledEvent domainEvent) {
    String orderId = domainEvent.getOrder().getId().toString();
    log.info("Received OrderCancelledEvent for order id: {}", orderId);
    try {
      PaymentRequestAvroModel paymentRequestAvroModel = orderMessagingDataMapper
          .orderCancelledEventToPaymentRequestAvroModel(domainEvent);

      kafkaProducer.send(orderServiceConfig.getPaymentRequestTopicName(),
          orderId,
          paymentRequestAvroModel,
          orderKafkaMessageHelper.
              getKafkaCallBack(orderServiceConfig.getPaymentRequestTopicName(),
                  paymentRequestAvroModel, orderId, "PaymentRequestAvroModel")
      );

      log.info("PaymentRequestAvroModel sent to Kafka for order id: {}",
          paymentRequestAvroModel.getOrderId());
    } catch (Exception ex) {
      log.error("Error while sending PaymentRequestAvroModel message"
      + "to kafka with order id: {}, error: {}", orderId, ex.getMessage());
    }
  }
}
