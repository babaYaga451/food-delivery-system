package com.food.ordering.system.order.service.data.access.mongo.outbox.restaurantApproval.repository;

import com.food.ordering.system.order.service.data.access.mongo.outbox.restaurantApproval.entity.ApprovalOutboxEntity;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalOutboxMongoRepository extends MongoRepository<ApprovalOutboxEntity, UUID> {

  Optional<List<ApprovalOutboxEntity>> findByTypeAndOutboxStatusAndSagaStatusIn(String type,
      OutboxStatus outboxStatus,
      List<SagaStatus> sagaStatus);

  Optional<ApprovalOutboxEntity> findByTypeAndSagaIdAndSagaStatusIn(String type,
      UUID sagaId,
      List<SagaStatus> sagaStatus);

  void deleteByTypeAndOutboxStatusAndSagaStatusIn(String type,
      OutboxStatus outboxStatus,
      List<SagaStatus> sagaStatus);

}
