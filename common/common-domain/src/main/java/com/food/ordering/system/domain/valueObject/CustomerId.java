package com.food.ordering.system.domain.valueObject;

import java.util.UUID;

public class CustomerId extends com.food.ordering.system.domain.valueObject.BaseId<UUID> {

  public CustomerId(UUID value) {
    super(value);
  }
}
