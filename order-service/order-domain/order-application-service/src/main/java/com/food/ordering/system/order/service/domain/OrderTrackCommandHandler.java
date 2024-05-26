package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueObject.TrackingId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderTrackCommandHandler {

  private final OrderDataMapper orderDataMapper;
  private final OrderRepository orderRepository;

  public OrderTrackCommandHandler(OrderDataMapper orderDataMapper,
      OrderRepository orderRepository) {
    this.orderDataMapper = orderDataMapper;
    this.orderRepository = orderRepository;
  }

  public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
    return orderRepository.findByTrackingId(new TrackingId(trackOrderQuery.getOrderTrackingId()))
        .map(orderDataMapper::orderToTrackOrderResponse)
        .orElseThrow(() -> {
          log.warn("Could not find order with id: {}", trackOrderQuery.getOrderTrackingId());
          return new OrderNotFoundException("Could not find order with id: " +
              trackOrderQuery.getOrderTrackingId());
        });
  }
}
