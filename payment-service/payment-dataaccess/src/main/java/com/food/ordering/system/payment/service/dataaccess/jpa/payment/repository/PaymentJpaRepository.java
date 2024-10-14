package com.food.ordering.system.payment.service.dataaccess.jpa.payment.repository;

import com.food.ordering.system.payment.service.dataaccess.jpa.payment.entity.PaymentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

  Optional<PaymentEntity> findByOrderId(UUID orderId);

}
