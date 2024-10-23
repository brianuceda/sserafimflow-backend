package com.brianuceda.sserafimflow.respositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.brianuceda.sserafimflow.entities.BankEntity;

@Repository
public interface BankRepository extends JpaRepository<BankEntity, Long> {
  Optional<BankEntity> findById(Long id);
  Optional<BankEntity> findByUsername(String username);
}
