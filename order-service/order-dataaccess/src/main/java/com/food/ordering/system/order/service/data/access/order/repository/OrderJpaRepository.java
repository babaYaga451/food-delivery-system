package com.food.ordering.system.order.service.data.access.order.repository;

import com.food.ordering.system.order.service.data.access.order.entity.OrderEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
  Optional<OrderEntity> findByTrackingId(UUID trackingId);
}
