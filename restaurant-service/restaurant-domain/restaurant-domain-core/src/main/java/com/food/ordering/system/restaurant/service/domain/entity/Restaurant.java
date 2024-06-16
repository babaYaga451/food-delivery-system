package com.food.ordering.system.restaurant.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.valueObject.OrderApprovalId;
import java.util.List;
import java.util.UUID;

public class Restaurant extends AggregateRoot<RestaurantId> {
  private OrderApproval orderApproval;
  private boolean active;
  private final OrderDetail orderDetail;

  public void validateOrder(List<String> failureMessages) {
    if (orderDetail.getOrderStatus() != OrderStatus.PAID) {
      failureMessages.add("Payment is not completed for order: " +
          orderDetail.getId().getValue().toString());
    }
    Money totalAmount = orderDetail.getProducts().stream()
        .map(product -> checkProductAvailableAndGetTotalAmount(failureMessages, product))
        .reduce(Money.ZERO, Money::add);
    if (totalAmount.getAmount().compareTo(orderDetail.getTotalAmount().getAmount()) != 0) {
      failureMessages.add("Total price is not correct for order: "+
          orderDetail.getId().getValue().toString());
    }
  }

  public void constructOrderApproval(OrderApprovalStatus orderApprovalStatus) {
    this.orderApproval = OrderApproval.Builder.builder()
        .orderApprovalId(new OrderApprovalId(UUID.randomUUID()))
        .orderId(this.orderDetail.getId())
        .restaurantId(this.getId())
        .approvalStatus(orderApprovalStatus)
        .build();
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  private Money checkProductAvailableAndGetTotalAmount(List<String> failureMessages,
      Product product) {
    if (!product.isAvailable()) {
      failureMessages.add("Product with id: "+ product.getId().getValue().toString()
      + " is not available");
    }
    return product.getPrice().multiply(product.getQuantity());
  }

  private Restaurant(Builder builder) {
    setId(builder.restaurantId);
    orderApproval = builder.orderApproval;
    active = builder.active;
    orderDetail = builder.orderDetail;
  }

  public OrderApproval getOrderApproval() {
    return orderApproval;
  }

  public boolean isActive() {
    return active;
  }

  public OrderDetail getOrderDetail() {
    return orderDetail;
  }

  public static final class Builder {

    private RestaurantId restaurantId;
    private OrderApproval orderApproval;
    private boolean active;
    private OrderDetail orderDetail;

    private Builder() {
    }

    public static Builder builder() {
      return new Builder();
    }

    public Builder restaurantId(RestaurantId val) {
      restaurantId = val;
      return this;
    }

    public Builder orderApproval(OrderApproval val) {
      orderApproval = val;
      return this;
    }

    public Builder active(boolean val) {
      active = val;
      return this;
    }

    public Builder orderDetail(OrderDetail val) {
      orderDetail = val;
      return this;
    }

    public Restaurant build() {
      return new Restaurant(this);
    }
  }
}
