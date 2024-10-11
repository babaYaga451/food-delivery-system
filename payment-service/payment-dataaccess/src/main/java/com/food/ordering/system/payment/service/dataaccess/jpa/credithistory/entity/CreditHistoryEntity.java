package com.food.ordering.system.payment.service.dataaccess.jpa.credithistory.entity;

import com.food.ordering.system.payment.service.domain.valueObject.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "credit_history")
public class CreditHistoryEntity {

  @Id
  private UUID id;
  private UUID customerId;
  private BigDecimal amount;
  @Enumerated(EnumType.STRING)
  private TransactionType type;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreditHistoryEntity that = (CreditHistoryEntity) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
