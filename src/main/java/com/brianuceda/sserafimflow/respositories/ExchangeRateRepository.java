package com.brianuceda.sserafimflow.respositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.brianuceda.sserafimflow.entities.ExchangeRateEntity;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, Long> {
  @SuppressWarnings("null")
  Optional<ExchangeRateEntity> findById(Long id);
  ExchangeRateEntity findByDate(LocalDate date);
}
