package com.food.ordering.system.payment.service.dataaccess.jpa.credithistory.adapter;

import com.food.ordering.system.payment.service.dataaccess.jpa.credithistory.mapper.CreditHistoryDataAccessMapper;
import com.food.ordering.system.payment.service.dataaccess.jpa.credithistory.repository.CreditHistoryJpaRepository;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CreditHistoryRepositoryImpl implements CreditHistoryRepository {

  private final CreditHistoryJpaRepository creditHistoryJpaRepository;
  private final CreditHistoryDataAccessMapper creditHistoryDataAccessMapper;

  public CreditHistoryRepositoryImpl(CreditHistoryJpaRepository creditHistoryJpaRepository,
      CreditHistoryDataAccessMapper creditHistoryDataAccessMapper) {
    this.creditHistoryJpaRepository = creditHistoryJpaRepository;
    this.creditHistoryDataAccessMapper = creditHistoryDataAccessMapper;
  }

  @Override
  public CreditHistory save(CreditHistory creditHistory) {
    return creditHistoryDataAccessMapper
        .creditHistoryEntityToCreditHistory(creditHistoryJpaRepository
            .save(creditHistoryDataAccessMapper.creditHistoryToCreditHistoryEntity(creditHistory)));
  }

  @Override
  public Optional<List<CreditHistory>> findByCustomerId(UUID customerId) {
    return creditHistoryJpaRepository.findByCustomerId(customerId)
        .map(creditHistoryEntities -> creditHistoryEntities.stream()
            .map(creditHistoryDataAccessMapper::creditHistoryEntityToCreditHistory)
            .collect(Collectors.toList())
        );
  }
}
