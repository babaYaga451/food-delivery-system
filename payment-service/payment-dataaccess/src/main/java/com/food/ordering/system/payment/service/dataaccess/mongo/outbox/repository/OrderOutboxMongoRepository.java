package com.food.ordering.system.payment.service.dataaccess.mongo.outbox.repository;

import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.dataaccess.mongo.outbox.entity.OrderOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderOutboxMongoRepository extends MongoRepository<OrderOutboxEntity, UUID> {

  Optional<List<OrderOutboxEntity>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);

  Optional<OrderOutboxEntity> findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(String type,
      UUID sagaId,
      PaymentStatus paymentStatus,
      OutboxStatus outboxStatus);

  void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);

}