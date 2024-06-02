package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfig;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantApproval.OrderPaidRestaurantRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PayOrderKafkaMessagePublisher implements OrderPaidRestaurantRequestMessagePublisher {

  private final OrderMessagingDataMapper orderMessagingDataMapper;
  private final OrderServiceConfig orderServiceConfig;
  private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;
  private final OrderKafkaMessageHelper orderKafkaMessageHelper;

  public PayOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper,
      OrderServiceConfig orderServiceConfig,
      KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer,
      OrderKafkaMessageHelper orderKafkaMessageHelper) {
    this.orderMessagingDataMapper = orderMessagingDataMapper;
    this.orderServiceConfig = orderServiceConfig;
    this.kafkaProducer = kafkaProducer;
    this.orderKafkaMessageHelper = orderKafkaMessageHelper;
  }

  @Override
  public void publish(OrderPaidEvent domainEvent) {
    String orderId = domainEvent.getOrder().getId().toString();
    log.info("Received OrderPaidEvent for order id: {}", orderId);
    try {
      RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel =
          orderMessagingDataMapper.orderPaidEventToRestaurantApprovalRequestAvroModel(domainEvent);

      kafkaProducer.send(orderServiceConfig.getRestaurantApprovalRequestTopicName(),
          orderId,
          restaurantApprovalRequestAvroModel,
          orderKafkaMessageHelper.
              getKafkaCallBack(orderServiceConfig.getPaymentRequestTopicName(),
                  restaurantApprovalRequestAvroModel, orderId,
                  "RestaurantApprovalRequestAvroModel")
      );

      log.info("RestaurantApprovalRequestAvroModel sent to Kafka for order id: {}",
          restaurantApprovalRequestAvroModel.getOrderId());
    } catch (Exception ex) {
      log.error("Error while sending RestaurantApprovalRequestAvroModel message"
          + "to kafka with order id: {}, error: {}", orderId, ex.getMessage());
    }
  }
}
