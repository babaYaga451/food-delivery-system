package com.food.ordering.system.payment.service.domain.valueObject;

import java.util.UUID;

public class CreditEntryId extends com.food.ordering.system.domain.valueObject.BaseId<UUID> {
  public CreditEntryId(UUID value) {
    super(value);
  }
}
