package com.brianuceda.sserafimflow.respositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.brianuceda.sserafimflow.entities.PurchaseEntity;
import com.brianuceda.sserafimflow.enums.StateEnum;

@Repository
public interface PurchaseRepository extends JpaRepository<PurchaseEntity, Long> {
  List<PurchaseEntity> findAllByDocumentCompanyId(Long companyId);
  List<PurchaseEntity> findAllByStateAndDocumentCompanyId(StateEnum status, Long companyId);
  List<PurchaseEntity> findAllByBankId(Long bankId);
  List<PurchaseEntity> findAllByStateAndBankId(StateEnum status, Long bankId);
  Optional<PurchaseEntity> findByIdAndBankId(Long id, Long bankId);

  List<PurchaseEntity> findAllByState(StateEnum status);
}
