package com.food.ordering.system.order.service.messaging.mapper;

import com.food.ordering.system.domain.valueObject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.Product;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantOrderStatus;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OrderMessagingDataMapper {

  public PaymentRequestAvroModel orderCreatedEventToPaymentRequestAvroModel(
      OrderCreatedEvent orderCreatedEvent) {
    Order order = orderCreatedEvent.getOrder();
    return PaymentRequestAvroModel.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSagaId("")
        .setOrderId(order.getId().getValue().toString())
        .setCustomerId(order.getCustomerId().getValue().toString())
        .setPrice(order.getPrice().getAmount())
        .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
        .setCreatedAt(orderCreatedEvent.getCreatedAt().toInstant())
        .build();
  }

  public PaymentRequestAvroModel orderCancelledEventToPaymentRequestAvroModel(
      OrderCancelledEvent orderCancelledEvent) {
    Order order = orderCancelledEvent.getOrder();
    return PaymentRequestAvroModel.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSagaId("")
        .setOrderId(order.getId().getValue().toString())
        .setCustomerId(order.getCustomerId().getValue().toString())
        .setPrice(order.getPrice().getAmount())
        .setPaymentOrderStatus(PaymentOrderStatus.CANCELLED)
        .setCreatedAt(orderCancelledEvent.getCreatedAt().toInstant())
        .build();
  }

  public RestaurantApprovalRequestAvroModel orderPaidEventToRestaurantApprovalRequestAvroModel(
      OrderPaidEvent orderPaidEvent) {
    Order order = orderPaidEvent.getOrder();
    return RestaurantApprovalRequestAvroModel.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setOrderId(order.getId().getValue().toString())
        .setRestaurantId(order.getRestaurantId().getValue().toString())
        .setSagaId("")
        .setRestaurantOrderStatus(RestaurantOrderStatus.valueOf(order.getOrderStatus().name()))
        .setProducts(order.getItems().stream()
            .map(orderItem -> Product.newBuilder()
                .setId(orderItem.getProduct().getId().getValue().toString())
                .setQuantity(orderItem.getQuantity())
                .build())
            .collect(Collectors.toList()))
        .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
        .build();
  }

  public PaymentResponse paymentResponseAvroModelToPaymentResponse(
      PaymentResponseAvroModel paymentResponseAvroModel) {
    return PaymentResponse.builder()
        .id(paymentResponseAvroModel.getId().toString())
        .paymentId(paymentResponseAvroModel.getPaymentId().toString())
        .sageId(paymentResponseAvroModel.getSagaId().toString())
        .customerId(paymentResponseAvroModel.getCustomerId().toString())
        .orderId(paymentResponseAvroModel.getOrderId().toString())
        .price(paymentResponseAvroModel.getPrice())
        .createdAt(paymentResponseAvroModel.getCreatedAt())
        .paymentStatus(PaymentStatus.valueOf(paymentResponseAvroModel.getPaymentStatus().name()))
        .failureMessages(paymentResponseAvroModel.getFailureMessages())
        .build();
  }

  public RestaurantApprovalResponse restaurantApprovalResponseAvroModelToRestaurantApprovalResponse(
      RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel) {
    return RestaurantApprovalResponse.builder()
        .id(restaurantApprovalResponseAvroModel.getId().toString())
        .restaurantId(restaurantApprovalResponseAvroModel.getRestaurantId().toString())
        .orderId(restaurantApprovalResponseAvroModel.getOrderId().toString())
        .sagaId(restaurantApprovalResponseAvroModel.getSagaId().toString())
        .createdAt(restaurantApprovalResponseAvroModel.getCreatedAt())
        .failureMessages(restaurantApprovalResponseAvroModel.getFailureMessages())
        .orderApprovalStatus(OrderApprovalStatus.valueOf(
            restaurantApprovalResponseAvroModel.getOrderApprovalStatus().name()))
        .build();
  }
}
