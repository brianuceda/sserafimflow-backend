package com.brianuceda.sserafimflow.respositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.brianuceda.sserafimflow.entities.ExchangeRateEntity;

import java.time.LocalDate;


@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, Long> {
  ExchangeRateEntity findByDate(LocalDate date);
}
