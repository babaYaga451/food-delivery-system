package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCreateCommandHandler {

  private final OrderDomainService orderDomainService;
  private final CustomerRepository customerRepository;
  private final OrderRepository orderRepository;
  private final RestaurantRepository restaurantRepository;
  private final OrderDataMapper orderDataMapper;
  private final ApplicationDomainEventPublisher applicationDomainEventPublisher;

  public OrderCreateCommandHandler(OrderDomainService orderDomainService,
      CustomerRepository customerRepository, OrderRepository orderRepository,
      RestaurantRepository restaurantRepository, OrderDataMapper orderDataMapper,
      ApplicationDomainEventPublisher applicationDomainEventPublisher) {
    this.orderDomainService = orderDomainService;
    this.customerRepository = customerRepository;
    this.orderRepository = orderRepository;
    this.restaurantRepository = restaurantRepository;
    this.orderDataMapper = orderDataMapper;
    this.applicationDomainEventPublisher = applicationDomainEventPublisher;
  }

  public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
    checkCustomer(createOrderCommand.getCustomerId());
    Restaurant restaurant = checkRestaurant(createOrderCommand);
    Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
    OrderCreatedEvent orderCreatedEvent = orderDomainService
        .validateAndInitiateOrder(order, restaurant);
    Order orderResult = saveOrder(order);
    log.info("Order is created with id: {}", orderResult.getId().getValue());
    applicationDomainEventPublisher.publish(orderCreatedEvent);
    return orderDataMapper.orderToCreateOrderResponse(orderResult);
  }

  private Order saveOrder(Order order) {
    Order orderResult = orderRepository.save(order);
    if (orderResult == null) {
      log.error("Could not save order!");
      throw new OrderDomainException("Could not save order!");
    }
    log.info("Order saved with id: {}", order.getId().getValue());
    return orderResult;
  }

  private Restaurant checkRestaurant(CreateOrderCommand createOrderCommand) {
    Restaurant restaurant = orderDataMapper.createOrderCommandToRestaurant(createOrderCommand);
    return restaurantRepository.findRestaurantInformation(restaurant)
        .orElseThrow(() -> {
          log.warn("Could not find restaurant with id: {}", createOrderCommand.getRestaurantId());
          return new OrderDomainException("Could not find restaurant with id: " +
              createOrderCommand.getRestaurantId());
        });
  }

  private void checkCustomer(UUID customerId) {
    customerRepository.findCustomer(customerId)
        .orElseThrow(() -> {
          log.warn("Could not find customer with id: {}", customerId);
          return new OrderDomainException("Could not find customer with id: " + customerId);
        });
  }

}
