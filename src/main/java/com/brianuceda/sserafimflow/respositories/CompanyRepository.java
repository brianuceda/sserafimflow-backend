package com.brianuceda.sserafimflow.respositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.brianuceda.sserafimflow.entities.CompanyEntity;

import jakarta.persistence.Tuple;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
  Optional<CompanyEntity> findByUsername(String username);

  @Query(value = """
      SELECT
          p.nominal_value AS nominalValue,
          p.received_value AS receivedValue,
          (p.nominal_value - p.received_value) AS discountedValue,
          d.document_type AS documentType,
          p.rate_type AS rateType,
          p.currency AS currency,
          d.portfolio_id AS portfolio,
          p.state AS state,
          EXTRACT(MONTH FROM p.purchase_date) AS month
      FROM
          purchase p
      JOIN
          document d ON p.document_id = d.id
      WHERE
          d.company_id = :companyId
          AND p.state = 'PAID' OR p.state = 'PENDING'
      ORDER BY
          p.purchase_date
      """, nativeQuery = true)
  List<Tuple> getDetailedPurchases(@Param("companyId") Long companyId);
}
