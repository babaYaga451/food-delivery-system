package com.food.ordering.system.order.service.domain.ports.output.repository;

import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.valueObject.TrackingId;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

  Order save(Order order);
  Optional<Order> findByTrackingId(TrackingId trackingId);

  Optional<Order> findById(OrderId orderId);

}
