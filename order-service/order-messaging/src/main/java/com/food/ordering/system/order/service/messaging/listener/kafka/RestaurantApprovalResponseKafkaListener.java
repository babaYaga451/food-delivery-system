package com.food.ordering.system.order.service.messaging.listener.kafka;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantApproval.RestaurantApprovalMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RestaurantApprovalResponseKafkaListener implements KafkaConsumer<RestaurantApprovalResponseAvroModel> {

  private final OrderMessagingDataMapper orderMessagingDataMapper;
  private final RestaurantApprovalMessageListener restaurantApprovalMessageListener;

  public RestaurantApprovalResponseKafkaListener(OrderMessagingDataMapper orderMessagingDataMapper,
      RestaurantApprovalMessageListener restaurantApprovalMessageListener) {
    this.orderMessagingDataMapper = orderMessagingDataMapper;
    this.restaurantApprovalMessageListener = restaurantApprovalMessageListener;
  }

  @Override
  @KafkaListener(
      groupId = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
      topics = "${order-service.restaurant-approval-response-topic-name}")
  public void receive(@Payload List<RestaurantApprovalResponseAvroModel> messages,
      @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
      @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
      @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
    log.info("{} no. of restaurant approval responses recieved with keys:{}, partitions: {},"
        + " offsets: {}",
        messages.size(),
        keys.toString(),
        partitions.toString(),
        offsets.toString());
    messages.forEach(restaurantApprovalResponseAvroModel -> {
      if (OrderApprovalStatus.APPROVED ==
          restaurantApprovalResponseAvroModel.getOrderApprovalStatus()) {
        log.info("Processing approved order for id: {}",
            restaurantApprovalResponseAvroModel.getOrderId());
        restaurantApprovalMessageListener.orderApproved(
            orderMessagingDataMapper.restaurantApprovalResponseAvroModelToRestaurantApprovalResponse(
                restaurantApprovalResponseAvroModel));
      } else if (OrderApprovalStatus.REJECTED ==
          restaurantApprovalResponseAvroModel.getOrderApprovalStatus()) {
        log.info("Processing rejected order for id: {} with failure message: {}",
            restaurantApprovalResponseAvroModel.getOrderId(),
            String.join(",",restaurantApprovalResponseAvroModel.getFailureMessages()));
        restaurantApprovalMessageListener.orderRejected(
            orderMessagingDataMapper.restaurantApprovalResponseAvroModelToRestaurantApprovalResponse(
                restaurantApprovalResponseAvroModel));
      }
    });
  }
}
