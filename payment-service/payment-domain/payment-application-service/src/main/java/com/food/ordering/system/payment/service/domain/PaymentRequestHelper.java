package com.food.ordering.system.payment.service.domain;

import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCancelledMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentFailedMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class PaymentRequestHelper {
  private final PaymentDomainService paymentDomainService;
  private final PaymentDataMapper paymentDataMapper;
  private final PaymentRepository paymentRepository;
  private final CreditEntryRepository creditEntryRepository;
  private final CreditHistoryRepository creditHistoryRepository;
  private final PaymentCompletedMessagePublisher paymentCompletedMessagePublisher;
  private final PaymentFailedMessagePublisher paymentFailedMessagePublisher;
  private final PaymentCancelledMessagePublisher paymentCancelledEventDomainEventPublisher;

  public PaymentRequestHelper(PaymentDomainService paymentDomainService,
      PaymentDataMapper paymentDataMapper, PaymentRepository paymentRepository,
      CreditEntryRepository creditEntryRepository,
      CreditHistoryRepository creditHistoryRepository,
      PaymentCompletedMessagePublisher paymentCompletedMessagePublisher,
      PaymentFailedMessagePublisher paymentFailedMessagePublisher,
      PaymentCancelledMessagePublisher paymentCancelledEventDomainEventPublisher) {
    this.paymentDomainService = paymentDomainService;
    this.paymentDataMapper = paymentDataMapper;
    this.paymentRepository = paymentRepository;
    this.creditEntryRepository = creditEntryRepository;
    this.creditHistoryRepository = creditHistoryRepository;
    this.paymentCompletedMessagePublisher = paymentCompletedMessagePublisher;
    this.paymentFailedMessagePublisher = paymentFailedMessagePublisher;
    this.paymentCancelledEventDomainEventPublisher = paymentCancelledEventDomainEventPublisher;
  }

  @Transactional
  public PaymentEvent persistPayment(PaymentRequest paymentRequest) {
    log.info("Received payment complete event for order id: {}", paymentRequest.getOrderId());
    Payment payment = paymentDataMapper.paymentRequestModelToPayment(paymentRequest);
    CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
    List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
    List<String> failureMessages = new ArrayList<>();
    PaymentEvent paymentEvent = paymentDomainService.validateAndInitiatePayment(payment,
        creditEntry,
        creditHistories,
        failureMessages,
        paymentCompletedMessagePublisher,
        paymentFailedMessagePublisher);
    persistDbObjects(payment, failureMessages, creditEntry, creditHistories);
    return paymentEvent;
  }

  @Transactional
  public PaymentEvent persistCancelPayment(PaymentRequest paymentRequest) {
    log.info("Received payment rollback event for order id: {}", paymentRequest.getOrderId());
    Payment payment = paymentRepository
        .findByOrderId(UUID.fromString(paymentRequest.getOrderId()))
        .orElseThrow(() -> {
          log.info("Payment not found for order id: {}", paymentRequest.getOrderId());
          return new PaymentApplicationServiceException("Payment not found for order id: "
              + paymentRequest.getOrderId());
        });
    CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
    List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
    List<String> failureMessages = new ArrayList<>();
    PaymentEvent paymentEvent = paymentDomainService.validateAndCancelPayment(payment,
        creditEntry,
        creditHistories,
        failureMessages,
        paymentFailedMessagePublisher,
        paymentCancelledEventDomainEventPublisher);
    persistDbObjects(payment, failureMessages, creditEntry, creditHistories);
    return paymentEvent;
  }

  private List<CreditHistory> getCreditHistory(
      com.food.ordering.system.domain.valueObject.CustomerId customerId) {
    return creditHistoryRepository.findByCustomerId(customerId.getValue())
        .orElseThrow(() -> {
          log.error("Could not find credit history for customer: {}", customerId.getValue());
          return new PaymentApplicationServiceException(
              "Could not find credit history for customer: " + customerId.getValue());
        });
  }

  private CreditEntry getCreditEntry(com.food.ordering.system.domain.valueObject.CustomerId
      customerId) {
    return creditEntryRepository.findByCustomerId(customerId.getValue())
        .orElseThrow(() -> {
          log.error("Could not find credit entry for customer: {}", customerId.getValue());
          return new PaymentApplicationServiceException(
              "Could not find credit entry for customer: " + customerId.getValue());
        });
  }

  private void persistDbObjects(Payment payment,
      List<String> failureMessages,
      CreditEntry creditEntry,
      List<CreditHistory> creditHistories) {
    paymentRepository.save(payment);
    if (failureMessages.isEmpty()) {
      creditEntryRepository.save(creditEntry);
      creditHistoryRepository.save(creditHistories.get(creditHistories.size() - 1));
    }
  }
}
