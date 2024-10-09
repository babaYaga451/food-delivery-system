package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfig;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantApproval.RestaurantApprovalRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderApprovalEventKafkaPublisher implements RestaurantApprovalRequestMessagePublisher {
  private final OrderMessagingDataMapper orderMessagingDataMapper;
  private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;
  private final OrderServiceConfig orderServiceConfig;
  private final KafkaMessageHelper kafkaMessageHelper;

  public OrderApprovalEventKafkaPublisher(OrderMessagingDataMapper orderMessagingDataMapper,
      KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer,
      OrderServiceConfig orderServiceConfig,
      KafkaMessageHelper kafkaMessageHelper) {
    this.orderMessagingDataMapper = orderMessagingDataMapper;
    this.kafkaProducer = kafkaProducer;
    this.orderServiceConfig = orderServiceConfig;
    this.kafkaMessageHelper = kafkaMessageHelper;
  }

  @Override
  public void publish(OrderApprovalOutboxMessage orderApprovalOutboxMessage,
      BiConsumer<OrderApprovalOutboxMessage, OutboxStatus> outboxCallback) {

    OrderApprovalEventPayload orderApprovalEventPayload =
        kafkaMessageHelper.getOrderEventPayload(
            orderApprovalOutboxMessage.getPayload(),
            OrderApprovalEventPayload.class);

    String sagaId = orderApprovalOutboxMessage.getSagaId().toString();
    log.info("Received OrderApprovalOutboxMessage for orderId {} and sagaId {}",
        orderApprovalEventPayload.getOrderId(),
        sagaId);
    try {
      RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel = orderMessagingDataMapper
          .orderApprovalEventToRestaurantApprovalRequestAvroModel(sagaId, orderApprovalEventPayload);

      kafkaProducer.send(
          orderServiceConfig.getRestaurantApprovalRequestTopicName(),
          sagaId,
          restaurantApprovalRequestAvroModel,
          kafkaMessageHelper.getKafkaCallBack(orderServiceConfig.getRestaurantApprovalRequestTopicName(),
              restaurantApprovalRequestAvroModel,
              orderApprovalOutboxMessage,
              outboxCallback,
              orderApprovalEventPayload.getOrderId(),
              "RestaurantApprovalRequestAvroModel"));

      log.info("OrderApprovalEventPayload sent to kafka for orderId: {} and sagaId: {}",
          orderApprovalEventPayload.getOrderId(), sagaId);

    } catch (Exception e) {
      log.error("OrderApprovalEventPayload sent to kafka failed for orderId: {} sagaId: {}, error: {}",
          orderApprovalEventPayload.getOrderId(), sagaId, e.getMessage());
    }

  }
}
