package com.food.ordering.system.order.service.data.access.outbox.restaurantApproval.entity;

import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "restaurant_approval_outbox")
public class ApprovalOutboxEntity {

  @Id
  private UUID id;
  private UUID sagaId;
  private ZonedDateTime createdAt;
  private ZonedDateTime processedAt;
  private String type;
  private String payload;
  @Enumerated(EnumType.STRING)
  private SagaStatus sagaStatus;
  @Enumerated(EnumType.STRING)
  private OrderStatus orderStatus;
  @Enumerated(EnumType.STRING)
  private OutboxStatus outboxStatus;
  @Version
  private int version;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApprovalOutboxEntity that = (ApprovalOutboxEntity) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

}
