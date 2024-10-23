package com.brianuceda.sserafimflow.respositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.brianuceda.sserafimflow.entities.DocumentEntity;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
  Optional<DocumentEntity> findByIdAndCompanyId(Long id, Long companyId);
  List<DocumentEntity> findAllByPortfolioId(Long portfolioId);
}
