package com.food.ordering.system.payment.service.dataaccess.mongo.outbox.exception;

public class OrderOutboxNotFoundException extends RuntimeException {

  public OrderOutboxNotFoundException(String message) {
    super(message);
  }
}