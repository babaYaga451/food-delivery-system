package com.food.ordering.system.order.service.data.access.order.adapter;

import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.order.service.data.access.order.mapper.OrderDataAccessMapper;
import com.food.ordering.system.order.service.data.access.order.repository.OrderJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueObject.TrackingId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OrderRepositoryImpl implements OrderRepository {
  private final OrderJpaRepository orderJpaRepository;
  private final OrderDataAccessMapper orderDataAccessMapper;

  public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository,
      OrderDataAccessMapper orderDataAccessMapper) {
    this.orderJpaRepository = orderJpaRepository;
    this.orderDataAccessMapper = orderDataAccessMapper;
  }

  @Override
  public Order save(Order order) {
    return orderDataAccessMapper.orderEntityToOrder(
        orderJpaRepository.save(orderDataAccessMapper.orderToOrderEntity(order)));
  }

  @Override
  public Optional<Order> findByTrackingId(TrackingId trackingId) {
    return orderJpaRepository.findByTrackingId(trackingId.getValue())
        .map(orderDataAccessMapper::orderEntityToOrder);
  }

  @Override
  public Optional<Order> findById(OrderId orderId) {
    return orderJpaRepository.findById(orderId.getValue())
        .map(orderDataAccessMapper::orderEntityToOrder);
  }
}
