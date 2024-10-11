package com.food.ordering.system.order.service.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableJpaRepositories(basePackages = {
    "com.food.ordering.system.order.service.data.access.jpa",
    "com.food.ordering.system.dataaccess"
})
@EntityScan(basePackages = {
    "com.food.ordering.system.order.service.data.access",
    "com.food.ordering.system.dataaccess"
})
@EnableMongoRepositories(basePackages = {
    "com.food.ordering.system.order.service.data.access.mongo",
})
@SpringBootApplication(scanBasePackages = "com.food.ordering.system")
@EnableAsync
public class OrderServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(OrderServiceApplication.class, args);
  }
}
