package com.food.ordering.system.restaurant.service.dataaccess.entity;

import com.food.ordering.system.domain.valueObject.OrderApprovalStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "order_approval", schema = "restaurant")
public class OrderApprovalEntity {

  @Id
  private UUID id;
  private UUID restaurantId;
  private UUID orderId;
  @Enumerated(EnumType.STRING)
  private OrderApprovalStatus status;

}
