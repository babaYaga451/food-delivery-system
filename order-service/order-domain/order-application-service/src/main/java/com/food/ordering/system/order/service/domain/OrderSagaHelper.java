package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.saga.SagaStatus;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderSagaHelper {

  private final OrderRepository orderRepository;

  public OrderSagaHelper(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  public Order findOrder(String orderId) {
    return orderRepository.findById(new OrderId(UUID.fromString(orderId)))
        .orElseThrow(() -> {
          log.error("Order not found for id: {}", orderId);
          return new OrderNotFoundException("Order not found for id: "+ orderId);
        });
  }

  void saveOrder(Order order) {
    orderRepository.save(order);
  }

  SagaStatus orderStatusToSagaStatus(OrderStatus orderStatus) {
    return switch (orderStatus) {
      case PAID -> SagaStatus.PROCESSING;
      case APPROVED -> SagaStatus.SUCCEEDED;
      case CANCELLING -> SagaStatus.COMPENSATING;
      case CANCELLED -> SagaStatus.COMPENSATED;
      default -> SagaStatus.STARTED;
    };
  }
}
