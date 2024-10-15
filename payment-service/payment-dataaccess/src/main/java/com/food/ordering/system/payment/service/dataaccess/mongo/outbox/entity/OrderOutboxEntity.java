package com.food.ordering.system.payment.service.dataaccess.mongo.outbox.entity;

import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payment_outbox")
@CompoundIndex(def = "{'type': 1,'sagaId':1,'paymentStatus':1,'outboxStatus':1}", unique = true)
public class OrderOutboxEntity {

  @Id
  private UUID id;
  private UUID sagaId;
  private LocalDateTime createdAt;
  private LocalDateTime processedAt;
  private String type;
  private String payload;
  @Enumerated(EnumType.STRING)
  private OutboxStatus outboxStatus;
  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;
  @Version
  private int version;


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OrderOutboxEntity that = (OrderOutboxEntity) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
