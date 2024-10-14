package com.food.ordering.system.order.service.data.access.mongo.outbox.payment.entity;

import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Document(collection = "payment_outbox")
public class PaymentOutboxEntity {

  @Id
  private UUID id;
  private UUID sagaId;
  private LocalDateTime createdAt;
  private LocalDateTime processedAt;
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
    PaymentOutboxEntity that = (PaymentOutboxEntity) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
