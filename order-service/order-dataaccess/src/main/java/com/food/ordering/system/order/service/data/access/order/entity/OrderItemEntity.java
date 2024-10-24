package com.food.ordering.system.order.service.data.access.order.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IdClass(OrderItemEntityId.class)
@Entity
@Table(name = "order_items")
public class OrderItemEntity {

  @Id
  private long id;
  @Id
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "ORDER_ID")
  private OrderEntity order;
  private UUID productId;
  private BigDecimal price;
  private int quantity;
  private BigDecimal subTotal;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrderItemEntity that = (OrderItemEntity) o;
    return id == that.id && Objects.equals(order, that.order);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, order);
  }
}
