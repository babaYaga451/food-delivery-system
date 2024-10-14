package com.food.ordering.system.restaurant.service.dataaccess.restaurant.mongo.outbox.exception;

public class OrderOutboxNotFoundException extends RuntimeException {

    public OrderOutboxNotFoundException(String message) {
        super(message);
    }
}
