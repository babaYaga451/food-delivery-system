package com.food.ordering.system.order.service.domain.dto.create;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateOrderCommand {

  private final UUID customerId;
  private final UUID restaurantId;
  private final BigDecimal price;
  private final List<OrderItem> items;
  private final OrderAddress address;

}
