package xyz.brianuceda.sserafimflow.respositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import xyz.brianuceda.sserafimflow.entities.BankEntity;

@Repository
public interface BankRepository extends JpaRepository<BankEntity, Long> {
  @SuppressWarnings("null")
  Optional<BankEntity> findById(Long id);
  Optional<BankEntity> findByUsername(String username);
}
