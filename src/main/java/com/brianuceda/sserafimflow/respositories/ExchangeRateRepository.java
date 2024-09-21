package com.brianuceda.sserafimflow.respositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

import com.brianuceda.sserafimflow.entities.ExchangeRateEntity;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, Long> {
  ExchangeRateEntity findByFecha(LocalDate fecha);
}
