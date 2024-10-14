package com.food.ordering.system.order.service.data.access.order.mapper;

import com.food.ordering.system.order.service.data.access.order.entity.OrderAddressEntity;
import com.food.ordering.system.order.service.data.access.order.entity.OrderEntity;
import com.food.ordering.system.order.service.data.access.order.entity.OrderItemEntity;
import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.ProductId;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.valueObject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueObject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueObject.TrackingId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OrderDataAccessMapper {

  public OrderEntity orderToOrderEntity(Order order) {
    OrderEntity orderEntity =  OrderEntity.builder()
        .id(order.getId().getValue())
        .customerId(order.getCustomerId().getValue())
        .restaurantId(order.getRestaurantId().getValue())
        .trackingId(order.getTrackingId().getValue())
        .address(deliveryAddressToAddressEntity(order.getDeliveryAddress()))
        .price(order.getPrice().getAmount())
        .orderItems(orderItemsToOrderItemEntity(order.getItems()))
        .orderStatus(order.getOrderStatus())
        .failureMessages(order.getFailureMessages() != null ?
            String.join(",", order.getFailureMessages()) : "")
        .build();
    orderEntity.getAddress().setOrder(orderEntity);
    orderEntity.getOrderItems().forEach(orderItemEntity -> orderItemEntity.setOrder(orderEntity));
    return orderEntity;
  }

  private List<OrderItemEntity> orderItemsToOrderItemEntity(List<OrderItem> items) {
    return items.stream()
        .map(orderItem -> OrderItemEntity.builder()
            .id(orderItem.getId().getValue())
            .productId(orderItem.getProduct().getId().getValue())
            .price(orderItem.getPrice().getAmount())
            .quantity(orderItem.getQuantity())
            .subTotal(orderItem.getSubTotal().getAmount())
            .build())
        .collect(Collectors.toList());
  }

  private OrderAddressEntity deliveryAddressToAddressEntity(StreetAddress deliveryAddress) {
    return OrderAddressEntity.builder()
        .id(deliveryAddress.getId())
        .street(deliveryAddress.getStreet())
        .city(deliveryAddress.getCity())
        .postalCode(deliveryAddress.getPostalCode())
        .build();
  }

  public Order orderEntityToOrder(OrderEntity orderEntity) {
    return Order.Builder.builder()
        .orderId(new OrderId(orderEntity.getId()))
        .customerId(new CustomerId(orderEntity.getCustomerId()))
        .restaurantId(new RestaurantId(orderEntity.getRestaurantId()))
        .deliveryAddress(addressEntityToDeliveryAddress(orderEntity.getAddress()))
        .price(new Money(orderEntity.getPrice()))
        .items(orderItemsEntityToOrderItem(orderEntity.getOrderItems()))
        .trackingId(new TrackingId(orderEntity.getTrackingId()))
        .orderStatus(orderEntity.getOrderStatus())
        .failureMessages(orderEntity.getFailureMessages().isEmpty() ? new ArrayList<>()
            : new ArrayList<>(Arrays.asList(orderEntity.getFailureMessages().split(","))))
        .build();
  }

  private List<OrderItem> orderItemsEntityToOrderItem(List<OrderItemEntity> items) {
    return items.stream()
        .map(orderItemEntity -> OrderItem.Builder.builder()
            .orderItemId(new OrderItemId(orderItemEntity.getId()))
            .product(new Product(new ProductId(orderItemEntity.getProductId())))
            .price(new Money(orderItemEntity.getPrice()))
            .quantity(orderItemEntity.getQuantity())
            .subTotal(new Money(orderItemEntity.getSubTotal()))
            .build())
        .collect(Collectors.toList());
  }

  private StreetAddress addressEntityToDeliveryAddress(OrderAddressEntity address) {
    return new StreetAddress(
        address.getId(),
        address.getStreet(),
        address.getPostalCode(),
        address.getCity());
  }

}
