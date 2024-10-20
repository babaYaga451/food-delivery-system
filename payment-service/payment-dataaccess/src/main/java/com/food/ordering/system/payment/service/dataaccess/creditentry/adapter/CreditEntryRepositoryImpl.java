package com.food.ordering.system.payment.service.dataaccess.creditentry.adapter;

import com.food.ordering.system.payment.service.dataaccess.creditentry.mapper.CreditEntryDataAccessMapper;
import com.food.ordering.system.payment.service.dataaccess.creditentry.repository.CreditEntryJpaRepository;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CreditEntryRepositoryImpl implements CreditEntryRepository {

  private final CreditEntryJpaRepository creditEntryJpaRepository;
  private final CreditEntryDataAccessMapper creditEntryDataAccessMapper;

  public CreditEntryRepositoryImpl(CreditEntryJpaRepository creditEntryJpaRepository,
      CreditEntryDataAccessMapper creditEntryDataAccessMapper) {
    this.creditEntryJpaRepository = creditEntryJpaRepository;
    this.creditEntryDataAccessMapper = creditEntryDataAccessMapper;
  }

  @Override
  public CreditEntry save(CreditEntry creditEntry) {
    return creditEntryDataAccessMapper
        .creditEntryEntityToCreditEntry(creditEntryJpaRepository
            .save(creditEntryDataAccessMapper.creditEntryToCreditEntryEntity(creditEntry)));
  }

  @Override
  public Optional<CreditEntry> findByCustomerId(UUID customerId) {
    return creditEntryJpaRepository.findByCustomerId(customerId)
        .map(creditEntryDataAccessMapper::creditEntryEntityToCreditEntry);
  }
}
