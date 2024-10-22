package com.food.ordering.system.payment.service.messaging.listener.kafka;

import com.food.ordering.system.kafka.consumer.KafkaSingleItemConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentRequestKafkaListener implements KafkaSingleItemConsumer<PaymentRequestAvroModel> {

  private final PaymentRequestMessageListener paymentRequestMessageListener;
  private final PaymentMessagingDataMapper paymentMessagingDataMapper;

  public PaymentRequestKafkaListener(PaymentRequestMessageListener paymentRequestMessageListener,
      PaymentMessagingDataMapper paymentMessagingDataMapper) {
    this.paymentRequestMessageListener = paymentRequestMessageListener;
    this.paymentMessagingDataMapper = paymentMessagingDataMapper;
  }

  @Override
  @KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}",
      topics = "${payment-service.payment-request-topic-name}")
  public void receive(@Payload PaymentRequestAvroModel paymentRequestAvroModel,
      @Header(KafkaHeaders.RECEIVED_KEY) String key,
      @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
      @Header(KafkaHeaders.OFFSET) Long offset) {

    log.info("{} payment requests recieved with key:{}, partition:{} and offset: {}",
        paymentRequestAvroModel,
        key,
        partition,
        offset);
    try {
      if (PaymentOrderStatus.PENDING == paymentRequestAvroModel.getPaymentOrderStatus()) {
        log.info("Processing payment for order id: {}", paymentRequestAvroModel.getOrderId());
        paymentRequestMessageListener.completePayment(paymentMessagingDataMapper
            .paymentRequestAvroModelToPaymentRequest(paymentRequestAvroModel));
      } else if (PaymentOrderStatus.CANCELLED == paymentRequestAvroModel.getPaymentOrderStatus()) {
        log.info("Cancelling payment for order id: {}", paymentRequestAvroModel.getOrderId());
        paymentRequestMessageListener.cancelPayment(paymentMessagingDataMapper
            .paymentRequestAvroModelToPaymentRequest(paymentRequestAvroModel));
      }
    } catch (DataAccessException e) {
      SQLException sqlException = (SQLException) e.getRootCause();
      if (sqlException != null && sqlException.getSQLState() != null &&
          PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
        //NO-OP for unique constraint exception
        log.error("Caught unique constraint exception with sql state: {} " +
                "in PaymentRequestKafkaListener for order id: {}",
            sqlException.getSQLState(), paymentRequestAvroModel.getOrderId());
      } else {
        throw new PaymentApplicationServiceException("Throwing DataAccessException in" +
            " PaymentRequestKafkaListener: " + e.getMessage(), e);
      }
    } catch (PaymentNotFoundException e) {
      //NO-OP for PaymentNotFoundException
      log.error("No payment found for order id: {}", paymentRequestAvroModel.getOrderId());
    }
  }
}
