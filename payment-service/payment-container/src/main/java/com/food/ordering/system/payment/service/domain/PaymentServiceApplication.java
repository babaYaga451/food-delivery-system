package com.food.ordering.system.payment.service.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableJpaRepositories(basePackages = "com.food.ordering.system.payment.service.dataaccess.jpa")
@EntityScan(basePackages = "com.food.ordering.system.payment.service.dataaccess")
@EnableMongoRepositories(basePackages = "com.food.ordering.system.payment.service.dataaccess.mongo")
@SpringBootApplication(scanBasePackages = "com.food.ordering.system")
public class PaymentServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(PaymentServiceApplication.class, args);
  }
}
