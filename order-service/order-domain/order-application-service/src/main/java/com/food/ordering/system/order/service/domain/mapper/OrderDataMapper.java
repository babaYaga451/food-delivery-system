package com.food.ordering.system.order.service.domain.mapper;

import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.PaymentOrderStatus;
import com.food.ordering.system.domain.valueObject.ProductId;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.domain.valueObject.RestaurantOrderStatus;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventProduct;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.valueObject.StreetAddress;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OrderDataMapper {

  public Restaurant createOrderCommandToRestaurant(CreateOrderCommand createOrderCommand) {
    return Restaurant.Builder.builder()
        .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
        .products(createOrderCommand.getItems().stream()
            .map(orderItem -> new Product(new ProductId(orderItem.getProductId())))
            .collect(Collectors.toList())
        )
        .build();
  }

  public Order createOrderCommandToOrder(CreateOrderCommand createOrderCommand) {
    return Order.Builder.builder()
        .customerId(new CustomerId(createOrderCommand.getCustomerId()))
        .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
        .deliveryAddress(orderAddressToDeliveryAddress(createOrderCommand.getAddress()))
        .price(new Money(createOrderCommand.getPrice()))
        .items(orderItemsToEntities(createOrderCommand.getItems()))
        .build();
  }

  private List<OrderItem> orderItemsToEntities(
      List<com.food.ordering.system.order.service.domain.dto.create.OrderItem> items) {
    return items.stream()
        .map(orderItem ->
            OrderItem.Builder.builder()
                .product(new Product(new ProductId(orderItem.getProductId())))
                .price(new Money(orderItem.getPrice()))
                .quantity(orderItem.getQuantity())
                .subTotal(new Money(orderItem.getSubTotal()))
                .build())
        .collect(Collectors.toList());
  }

  private StreetAddress orderAddressToDeliveryAddress(OrderAddress address) {
    return new StreetAddress(
        UUID.randomUUID(),
        address.getStreet(),
        address.getPostalCode(),
        address.getCity()
    );
  }

  public CreateOrderResponse orderToCreateOrderResponse(Order orderResult, String message) {
    return CreateOrderResponse.builder()
        .trackingId(orderResult.getTrackingId().getValue())
        .orderStatus(orderResult.getOrderStatus())
        .message(message)
        .build();
  }

  public TrackOrderResponse orderToTrackOrderResponse(Order order) {
    return TrackOrderResponse.builder()
        .orderTrackingId(order.getTrackingId().getValue())
        .orderStatus(order.getOrderStatus())
        .failureMessages(order.getFailureMessages())
        .build();
  }

  public OrderPaymentEventPayload orderCreatedEventToOrderPaymentEventPayload(
      OrderCreatedEvent orderCreatedEvent) {
    return OrderPaymentEventPayload.builder()
        .customerId(orderCreatedEvent.getOrder().getCustomerId().getValue().toString())
        .orderId(orderCreatedEvent.getOrder().getId().getValue().toString())
        .price(orderCreatedEvent.getOrder().getPrice().getAmount())
        .createdAt(orderCreatedEvent.getCreatedAt())
        .paymentOrderStatus(PaymentOrderStatus.PENDING.name())
        .build();
  }

  public OrderApprovalEventPayload orderPaidEventToOrderApprovalEventPayload(
      OrderPaidEvent orderPaidEvent) {
    return OrderApprovalEventPayload.builder()
        .orderId(orderPaidEvent.getOrder().getId().getValue().toString())
        .restaurantId(orderPaidEvent.getOrder().getRestaurantId().getValue().toString())
        .restaurantOrderStatus(RestaurantOrderStatus.PAID.name())
        .products(orderPaidEvent.getOrder().getItems()
            .stream()
            .map(orderItem -> OrderApprovalEventProduct.builder()
                .id(orderItem.getProduct().getId().getValue().toString())
                .quantity(orderItem.getQuantity())
                .build())
            .collect(Collectors.toList()))
        .price(orderPaidEvent.getOrder().getPrice().getAmount())
        .createdAt(orderPaidEvent.getCreatedAt())
        .build();
  }

  public OrderPaymentEventPayload orderCancelledEventToOrderPaymentEventPayload(
      OrderCancelledEvent orderCancelledEvent) {
    return OrderPaymentEventPayload.builder()
        .orderId(orderCancelledEvent.getOrder().getId().getValue().toString())
        .customerId(orderCancelledEvent.getOrder().getCustomerId().getValue().toString())
        .price(orderCancelledEvent.getOrder().getPrice().getAmount())
        .createdAt(orderCancelledEvent.getCreatedAt())
        .paymentOrderStatus(PaymentOrderStatus.CANCELLED.name())
        .build();
  }
}
