package com.food.ordering.system.restaurant.service.domain.mapper;

import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.domain.valueObject.ProductId;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.OrderDetail;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RestaurantDataMapper {

  public Restaurant restaurantApprovalRequestToRestaurant(
      RestaurantApprovalRequest restaurantApprovalRequest) {
    return Restaurant.Builder.builder()
        .restaurantId(new RestaurantId(UUID.fromString(restaurantApprovalRequest.getId())))
        .orderDetail(OrderDetail.Builder.builder()
            .orderId(new OrderId(UUID.fromString(restaurantApprovalRequest.getOrderId())))
            .products(restaurantApprovalRequest.getProducts().stream()
                .map(product -> Product.Builder.builder()
                    .productId(product.getId())
                    .quantity(product.getQuantity())
                    .build())
                .collect(Collectors.toList()))
            .totalAmount(new Money(restaurantApprovalRequest.getPrice()))
            .orderStatus(OrderStatus.valueOf(
                restaurantApprovalRequest.getRestaurantOrderStatus().name()))
            .build())
        .build();
  }
}
