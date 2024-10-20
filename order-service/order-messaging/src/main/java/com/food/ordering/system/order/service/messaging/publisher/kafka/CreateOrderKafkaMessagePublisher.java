package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfig;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreateOrderKafkaMessagePublisher implements
    OrderCreatedPaymentRequestMessagePublisher {

  private final OrderMessagingDataMapper orderMessagingDataMapper;
  private final OrderServiceConfig orderServiceConfig;
  private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
  private final KafkaMessageHelper kafkaMessageHelper;

  public CreateOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper,
      OrderServiceConfig orderServiceConfig,
      KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer,
      KafkaMessageHelper kafkaMessageHelper) {
    this.orderMessagingDataMapper = orderMessagingDataMapper;
    this.orderServiceConfig = orderServiceConfig;
    this.kafkaProducer = kafkaProducer;
    this.kafkaMessageHelper = kafkaMessageHelper;
  }

  @Override
  public void publish(OrderCreatedEvent orderCreatedEvent) {
    String orderId = orderCreatedEvent.getOrder().getId().toString();
    log.info("Received OrderCreatedEvent for order id: {}", orderId);
    try {
      PaymentRequestAvroModel paymentRequestAvroModel = orderMessagingDataMapper
          .orderCreatedEventToPaymentRequestAvroModel(orderCreatedEvent);

      kafkaProducer.send(orderServiceConfig.getPaymentRequestTopicName(),
          orderId,
          paymentRequestAvroModel,
          kafkaMessageHelper.
              getKafkaCallBack(orderServiceConfig.getPaymentRequestTopicName(),
                  paymentRequestAvroModel, orderId, "PaymentRequestAvroModel")
      );

      log.info("PaymentRequestAvroModel sent to Kafka for order id: {}",
          paymentRequestAvroModel.getOrderId());
    } catch (Exception ex) {
      log.error("Error while sending PaymentRequestAvroModel message "
      + "to kafka with order id: {}, error: {}", orderId, ex.getMessage());
    }
  }
}
