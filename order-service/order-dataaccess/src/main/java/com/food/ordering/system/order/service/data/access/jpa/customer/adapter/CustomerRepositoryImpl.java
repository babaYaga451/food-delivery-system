package com.food.ordering.system.order.service.data.access.jpa.customer.adapter;

import com.food.ordering.system.order.service.data.access.jpa.customer.mapper.CustomerDataAccessMapper;
import com.food.ordering.system.order.service.data.access.jpa.customer.repository.CustomerJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CustomerRepositoryImpl implements CustomerRepository {

  private final CustomerJpaRepository customerJpaRepository;
  private final CustomerDataAccessMapper customerDataAccessMapper;

  public CustomerRepositoryImpl(CustomerJpaRepository customerJpaRepository,
      CustomerDataAccessMapper customerDataAccessMapper) {
    this.customerJpaRepository = customerJpaRepository;
    this.customerDataAccessMapper = customerDataAccessMapper;
  }

  @Override
  public Optional<Customer> findCustomer(UUID customerId) {
    return customerJpaRepository.findById(customerId)
        .map(customerDataAccessMapper::customerEntityToCustomer);
  }
}
