package com.food.ordering.system.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.outbox.OutboxStatus;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaMessageHelper {

  private final ObjectMapper objectMapper;

  public KafkaMessageHelper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public <T, U> BiConsumer<SendResult<String, T>, Throwable>
  getKafkaCallBack(
      String topicName,
      T avroModel,
      U outboxMessage,
      BiConsumer<U, OutboxStatus> outBoxCallback,
      String orderId,
      String avroModelName) {

    return (result, ex) -> {
      if (ex != null) {
        log.error("Error while sending: {} with message: {} and outbox type: {} to topic: {}",
            avroModelName, avroModel.toString(),outboxMessage.getClass().getName(), topicName, ex);
        outBoxCallback.accept(outboxMessage, OutboxStatus.FAILED);
      } else {
        RecordMetadata metadata = result.getRecordMetadata();
        log.info("Received successful response from kafka for order id: {}" +
                "Topic: {} Partition: {} Offset: {} Timestamp: {}",
            orderId,
            metadata.topic(),
            metadata.partition(),
            metadata.offset(),
            metadata.timestamp());
        outBoxCallback.accept(outboxMessage, OutboxStatus.COMPLETED);
      }
    };
  }

  public <T> T getOrderEventPayload(
      String payload, Class<T> outputType) {
    try {
      return objectMapper.readValue(payload, outputType);
    } catch (JsonProcessingException e) {
      log.error("Could not read {}} object!",outputType.getName(), e);
      throw new OrderDomainException("Could not read "+ outputType.getName() + " object!",e);
    }
  }
}
