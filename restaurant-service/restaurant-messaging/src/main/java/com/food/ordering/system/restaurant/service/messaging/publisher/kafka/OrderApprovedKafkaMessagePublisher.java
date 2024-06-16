package com.food.ordering.system.restaurant.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.restaurant.service.domain.config.RestaurantServiceConfigData;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.OrderApprovedMessagePublisher;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderApprovedKafkaMessagePublisher implements OrderApprovedMessagePublisher {
  private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;
  private final RestaurantServiceConfigData restaurantServiceConfigData;
  private final KafkaMessageHelper kafkaMessageHelper;
  private final KafkaProducer<String, RestaurantApprovalResponseAvroModel> kafkaProducer;

  public OrderApprovedKafkaMessagePublisher(
      RestaurantMessagingDataMapper restaurantMessagingDataMapper,
      RestaurantServiceConfigData restaurantServiceConfigData,
      KafkaMessageHelper kafkaMessageHelper,
      KafkaProducer<String, RestaurantApprovalResponseAvroModel> kafkaProducer) {
    this.restaurantMessagingDataMapper = restaurantMessagingDataMapper;
    this.restaurantServiceConfigData = restaurantServiceConfigData;
    this.kafkaMessageHelper = kafkaMessageHelper;
    this.kafkaProducer = kafkaProducer;
  }

  @Override
  public void publish(OrderApprovedEvent domainEvent) {
    String orderId = domainEvent.getOrderApproval().getOrderId().getValue().toString();

    log.info("Received OrderApprovedEvent for order id: {}", orderId);
    try {
      RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel =
          restaurantMessagingDataMapper.orderApprovedEventToRestaurantApprovalResponseAvroModel(domainEvent);
      kafkaProducer.send(restaurantServiceConfigData.getRestaurantApprovalResponseTopicName(),
          orderId,
          restaurantApprovalResponseAvroModel,
          kafkaMessageHelper.getKafkaCallBack(
              restaurantServiceConfigData.getRestaurantApprovalResponseTopicName(),
              restaurantApprovalResponseAvroModel,
              orderId,
              "RestaurantApprovalResponseAvroModel"));
      log.info("RestaurantApprovalResponseAvroModel sent to kafka at {}", System.nanoTime());
    } catch (Exception e) {
      log.error("Error while sending RestaurantApprovalResponseAvroModel message "
          + " to kafka with order id: {}, error: {}", orderId, e.getMessage());
    }
  }
}
