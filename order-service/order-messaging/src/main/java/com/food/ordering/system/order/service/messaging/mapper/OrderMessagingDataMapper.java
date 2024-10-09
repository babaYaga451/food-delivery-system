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
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OrderMessagingDataMapper {

  public PaymentResponse paymentResponseAvroModelToPaymentResponse(
      PaymentResponseAvroModel paymentResponseAvroModel) {
    return PaymentResponse.builder()
        .id(paymentResponseAvroModel.getId())
        .paymentId(paymentResponseAvroModel.getPaymentId())
        .sagaId(paymentResponseAvroModel.getSagaId())
        .customerId(paymentResponseAvroModel.getCustomerId())
        .orderId(paymentResponseAvroModel.getOrderId())
        .price(paymentResponseAvroModel.getPrice())
        .createdAt(paymentResponseAvroModel.getCreatedAt())
        .paymentStatus(PaymentStatus.valueOf(paymentResponseAvroModel.getPaymentStatus().name()))
        .failureMessages(paymentResponseAvroModel.getFailureMessages())
        .build();
  }

  public RestaurantApprovalResponse restaurantApprovalResponseAvroModelToRestaurantApprovalResponse(
      RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel) {
    return RestaurantApprovalResponse.builder()
        .id(restaurantApprovalResponseAvroModel.getId())
        .restaurantId(restaurantApprovalResponseAvroModel.getRestaurantId())
        .orderId(restaurantApprovalResponseAvroModel.getOrderId())
        .sagaId(restaurantApprovalResponseAvroModel.getSagaId())
        .createdAt(restaurantApprovalResponseAvroModel.getCreatedAt())
        .failureMessages(restaurantApprovalResponseAvroModel.getFailureMessages())
        .orderApprovalStatus(OrderApprovalStatus.valueOf(
            restaurantApprovalResponseAvroModel.getOrderApprovalStatus().name()))
        .build();
  }

  public PaymentRequestAvroModel orderPaymentEventToPaymentRequestAvroModel(String sagaId,
      OrderPaymentEventPayload orderPaymentEventPayload) {
    return PaymentRequestAvroModel.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSagaId(sagaId)
        .setCustomerId(orderPaymentEventPayload.getCustomerId())
        .setOrderId(orderPaymentEventPayload.getOrderId())
        .setPrice(orderPaymentEventPayload.getPrice())
        .setCreatedAt(orderPaymentEventPayload.getCreatedAt().toInstant())
        .setPaymentOrderStatus(PaymentOrderStatus.valueOf(orderPaymentEventPayload.getPaymentOrderStatus()))
        .build();
  }

  public RestaurantApprovalRequestAvroModel
  orderApprovalEventToRestaurantApprovalRequestAvroModel(
      String sagaId, OrderApprovalEventPayload orderApprovalEventPayload) {
    return RestaurantApprovalRequestAvroModel.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setOrderId(orderApprovalEventPayload.getOrderId())
        .setRestaurantId(orderApprovalEventPayload.getRestaurantId())
        .setSagaId(sagaId)
        .setRestaurantOrderStatus(
            com.food.ordering.system.kafka.order.avro.model.RestaurantOrderStatus
                .valueOf(orderApprovalEventPayload.getRestaurantOrderStatus()))
        .setProducts(orderApprovalEventPayload.getProducts().stream()
            .map(orderApprovalEventProduct -> Product.newBuilder()
                .setId(orderApprovalEventProduct.getId())
                .setQuantity(orderApprovalEventProduct.getQuantity())
                .build())
            .collect(Collectors.toList()))
        .setPrice(orderApprovalEventPayload.getPrice())
        .setCreatedAt(orderApprovalEventPayload.getCreatedAt().toInstant())
        .build();
  }
}
