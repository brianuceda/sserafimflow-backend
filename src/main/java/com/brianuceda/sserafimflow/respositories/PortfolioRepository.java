package com.brianuceda.sserafimflow.respositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.brianuceda.sserafimflow.entities.PortfolioEntity;

@Repository
public interface PortfolioRepository extends JpaRepository<PortfolioEntity, Long> {
  Optional<PortfolioEntity> findByName(String name);
  List<PortfolioEntity> findAllByCompanyId(Long companyId);
}
