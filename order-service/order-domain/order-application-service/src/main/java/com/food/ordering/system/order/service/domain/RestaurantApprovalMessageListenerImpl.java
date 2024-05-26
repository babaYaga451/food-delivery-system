package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantApproval.RestaurantApprovalMessageListener;

public class RestaurantApprovalMessageListenerImpl implements RestaurantApprovalMessageListener {

  @Override
  public void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse) {

  }

  @Override
  public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse) {

  }
}
