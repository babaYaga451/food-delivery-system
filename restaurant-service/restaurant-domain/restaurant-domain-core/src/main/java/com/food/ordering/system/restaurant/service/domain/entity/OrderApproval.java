package com.food.ordering.system.restaurant.service.domain.entity;

import com.food.ordering.system.domain.entity.BaseEntity;
import com.food.ordering.system.domain.valueObject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.valueObject.OrderApprovalId;

public class OrderApproval extends BaseEntity<OrderApprovalId> {
  private final RestaurantId restaurantId;
  private final OrderId orderId;
  private final OrderApprovalStatus approvalStatus;

  private OrderApproval(Builder builder) {
    super.setId(builder.orderApprovalId);
    restaurantId = builder.restaurantId;
    orderId = builder.orderId;
    approvalStatus = builder.approvalStatus;
  }

  public RestaurantId getRestaurantId() {
    return restaurantId;
  }

  public OrderId getOrderId() {
    return orderId;
  }

  public OrderApprovalStatus getApprovalStatus() {
    return approvalStatus;
  }

  public static final class Builder {

    private OrderApprovalId orderApprovalId;
    private RestaurantId restaurantId;
    private OrderId orderId;
    private OrderApprovalStatus approvalStatus;

    private Builder() {
    }

    public static Builder builder() {
      return new Builder();
    }

    public Builder orderApprovalId(OrderApprovalId val) {
      orderApprovalId = val;
      return this;
    }

    public Builder restaurantId(RestaurantId val) {
      restaurantId = val;
      return this;
    }

    public Builder orderId(OrderId val) {
      orderId = val;
      return this;
    }

    public Builder approvalStatus(OrderApprovalStatus val) {
      approvalStatus = val;
      return this;
    }

    public OrderApproval build() {
      return new OrderApproval(this);
    }
  }
}
