package xyz.brianuceda.sserafimflow.respositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import xyz.brianuceda.sserafimflow.entities.ExchangeRateEntity;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, Long> {
  @SuppressWarnings("null")
  Optional<ExchangeRateEntity> findById(Long id);
  List<ExchangeRateEntity> findByDate(LocalDate date);
}
