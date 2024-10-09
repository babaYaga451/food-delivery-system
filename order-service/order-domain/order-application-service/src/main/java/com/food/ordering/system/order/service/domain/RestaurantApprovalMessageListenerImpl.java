package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantApproval.RestaurantApprovalMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class RestaurantApprovalMessageListenerImpl implements RestaurantApprovalMessageListener {

  private final OrderApprovalSaga orderApprovalSaga;

  public RestaurantApprovalMessageListenerImpl(OrderApprovalSaga orderApprovalSaga) {
    this.orderApprovalSaga = orderApprovalSaga;
  }


  @Override
  public void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse) {
    orderApprovalSaga.process(restaurantApprovalResponse);
    log.info("Order is approved for order id: {}", restaurantApprovalResponse.getOrderId());
  }

  @Override
  public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse) {
    orderApprovalSaga.rollback(restaurantApprovalResponse);
    log.info("Order approval rollback saga operation is completed for order id: {} with failure messages: {}",
        restaurantApprovalResponse.getOrderId(),
        String.join(",", restaurantApprovalResponse.getFailureMessages()));
  }
}
