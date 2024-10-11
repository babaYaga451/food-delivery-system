package com.food.ordering.system.order.service.data.access.jpa.customer.repository;

import com.food.ordering.system.order.service.data.access.jpa.customer.entity.CustomerEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {

}
