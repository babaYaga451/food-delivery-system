package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class OrderCreateHelper {

  private final OrderDomainService orderDomainService;
  private final CustomerRepository customerRepository;
  private final OrderRepository orderRepository;
  private final RestaurantRepository restaurantRepository;
  private final OrderDataMapper orderDataMapper;
  private final OrderCreatedPaymentRequestMessagePublisher orderCreatedEventDomainEventPublisher;

  public OrderCreateHelper(OrderDomainService orderDomainService,
      CustomerRepository customerRepository, OrderRepository orderRepository,
      RestaurantRepository restaurantRepository, OrderDataMapper orderDataMapper,
      OrderCreatedPaymentRequestMessagePublisher orderCreatedEventDomainEventPublisher) {
    this.orderDomainService = orderDomainService;
    this.customerRepository = customerRepository;
    this.orderRepository = orderRepository;
    this.restaurantRepository = restaurantRepository;
    this.orderDataMapper = orderDataMapper;
    this.orderCreatedEventDomainEventPublisher = orderCreatedEventDomainEventPublisher;
  }

  @Transactional
  public OrderCreatedEvent persistOrder(CreateOrderCommand createOrderCommand) {
    checkCustomer(createOrderCommand.getCustomerId());
    Restaurant restaurant = checkRestaurant(createOrderCommand);
    Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
    OrderCreatedEvent orderCreatedEvent = orderDomainService
        .validateAndInitiateOrder(order, restaurant, orderCreatedEventDomainEventPublisher);
    saveOrder(order);
    log.info("Order created with id: {}", orderCreatedEvent.getOrder().getId().getValue());
    return orderCreatedEvent;
  }

  private void saveOrder(Order order) {
    Order orderResult = orderRepository.save(order);
    if (orderResult == null) {
      log.error("Could not save order!");
      throw new OrderDomainException("Could not save order!");
    }
    log.info("Order saved with id: {}", order.getId().getValue());
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
