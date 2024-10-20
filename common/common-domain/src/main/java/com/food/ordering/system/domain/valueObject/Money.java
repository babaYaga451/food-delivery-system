package com.food.ordering.system.domain.valueObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

public class Money {

  private final BigDecimal amount;
  public static final Money ZERO = new Money(BigDecimal.ZERO);

  public Money(BigDecimal amount) {
    this.amount = amount;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public boolean isGreaterThanZero() {
    return Optional.ofNullable(this.amount)
        .filter(amount -> amount.compareTo(BigDecimal.ZERO) > 0)
        .isPresent();
  }

  public boolean isGreaterThan(Money money) {
    return Optional.ofNullable(this.amount)
        .filter(amount -> amount.compareTo(money.getAmount()) > 0)
        .isPresent();
  }

  public Money add(Money money) {
    return new Money(setScale(this.amount.add(money.getAmount())));
  }

  public Money subtract(Money money) {
    return new Money(setScale(this.amount.subtract(money.getAmount())));
  }

  public Money multiply(int multiplier) {
    return new Money(setScale(this.amount.multiply(new BigDecimal(multiplier))));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Money money = (Money) o;
    return Objects.equals(amount, money.amount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount);
  }

  private BigDecimal setScale(BigDecimal input) {
    return input.setScale(2, RoundingMode.HALF_EVEN);
  }
}
