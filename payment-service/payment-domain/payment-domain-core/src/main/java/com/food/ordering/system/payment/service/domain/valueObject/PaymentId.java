package com.food.ordering.system.payment.service.domain.valueObject;

import java.util.UUID;

public class PaymentId extends com.food.ordering.system.domain.valueObject.BaseId<UUID> {
  public PaymentId(UUID value) {
    super(value);
  }
}
