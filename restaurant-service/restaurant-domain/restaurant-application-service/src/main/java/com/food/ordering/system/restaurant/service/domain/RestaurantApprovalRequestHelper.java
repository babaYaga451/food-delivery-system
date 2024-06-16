package com.food.ordering.system.restaurant.service.domain;

import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.ProductId;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.food.ordering.system.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.OrderApprovedMessagePublisher;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.OrderRejectedMessagePublisher;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderApprovalRepository;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class RestaurantApprovalRequestHelper {

  private final RestaurantDomainService restaurantDomainService;
  private final RestaurantDataMapper restaurantDataMapper;
  private final RestaurantRepository restaurantRepository;
  private final OrderApprovalRepository orderApprovalRepository;
  private final OrderApprovedMessagePublisher orderApprovedMessagePublisher;
  private final OrderRejectedMessagePublisher orderRejectedMessagePublisher;

  public RestaurantApprovalRequestHelper(RestaurantDomainService restaurantDomainService,
      RestaurantDataMapper restaurantDataMapper, RestaurantRepository restaurantRepository,
      OrderApprovalRepository orderApprovalRepository,
      OrderApprovedMessagePublisher orderApprovedMessagePublisher,
      OrderRejectedMessagePublisher orderRejectedMessagePublisher) {
    this.restaurantDomainService = restaurantDomainService;
    this.restaurantDataMapper = restaurantDataMapper;
    this.restaurantRepository = restaurantRepository;
    this.orderApprovalRepository = orderApprovalRepository;
    this.orderApprovedMessagePublisher = orderApprovedMessagePublisher;
    this.orderRejectedMessagePublisher = orderRejectedMessagePublisher;
  }

  @Transactional
  public OrderApprovalEvent persistOrderApproval(
      RestaurantApprovalRequest restaurantApprovalRequest) {
    log.info("Processing restaurant approval for order id: {}",
        restaurantApprovalRequest.getOrderId());
    List<String> failureMessages = new ArrayList<>();
    Restaurant restaurant = findRestaurantInformation(restaurantApprovalRequest);
    OrderApprovalEvent orderApprovalEvent = restaurantDomainService.validateOrder(
        restaurant,
        failureMessages,
        orderApprovedMessagePublisher,
        orderRejectedMessagePublisher);
    orderApprovalRepository.save(restaurant.getOrderApproval());
    return orderApprovalEvent;
  }

  private Restaurant findRestaurantInformation(RestaurantApprovalRequest restaurantApprovalRequest) {
    Restaurant restaurant = restaurantDataMapper
        .restaurantApprovalRequestToRestaurant(restaurantApprovalRequest);
    Restaurant restaurantEntity = restaurantRepository.findRestaurantInformation(restaurant)
        .orElseThrow(() -> {
          log.error("Restaurant with id " + restaurant.getId().getValue().toString() + "not "
              + "found!");
          return new RestaurantNotFoundException("Restaurant with id " +
              restaurant.getId().getValue().toString() + "not found!");
        });

    Map<ProductId, Product> productMap = restaurantEntity.getOrderDetail().getProducts()
        .stream()
        .collect(Collectors.toMap(
            Product::getId,
            Function.identity(),
            (product1, product2) -> product1));

    restaurant.setActive(restaurantEntity.isActive());
    restaurant.getOrderDetail().getProducts()
        .stream()
        .filter(product -> productMap.containsKey(product.getId()))
        .forEach(product -> {
          Product p = productMap.get(product.getId());
          product.updateWithConfirmedNameAndPriceAndAvailability(p.getName(), p.getPrice(),
              p.isAvailable());
        });
    restaurant.getOrderDetail().setId(new OrderId(UUID.fromString(
        restaurantApprovalRequest.getOrderId())));
    return restaurant;
  }
}
