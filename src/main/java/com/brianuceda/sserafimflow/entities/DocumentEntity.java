package com.brianuceda.sserafimflow.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.brianuceda.sserafimflow.dtos.DocumentDTO;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.brianuceda.sserafimflow.enums.StateEnum;
import com.brianuceda.sserafimflow.enums.DocumentTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "document")
public class DocumentEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private DocumentTypeEnum documentType;

  @Column(precision = 16, scale = 4, nullable = false)
  @Positive
  private BigDecimal amount;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CurrencyEnum currency;

  @Column(nullable = false)
  private LocalDate issueDate;

  @Column(nullable = false)
  private LocalDate discountDate;

  @Column(nullable = false)
  private LocalDate expirationDate;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private StateEnum state; // Estado del documento

  @Column(nullable = false)
  private String clientName;

  @Column(nullable = true)
  private String clientPhone;

  @ManyToOne
  @JoinColumn(name = "company_id")
  private CompanyEntity company;

  @ManyToOne
  @JoinColumn(name = "portfolio_id")
  private PortfolioEntity portfolio;

  public DocumentEntity(DocumentDTO documentDTO) {
    this.id = documentDTO.getId();
    this.documentType = documentDTO.getDocumentType();
    this.amount = documentDTO.getAmount();
    this.currency = documentDTO.getCurrency();
    this.issueDate = documentDTO.getIssueDate();
    this.discountDate = documentDTO.getDiscountDate();
    this.expirationDate = documentDTO.getExpirationDate();
    this.clientName = documentDTO.getClientName();
    this.clientPhone = documentDTO.getClientPhone();
    this.state = documentDTO.getState();
  }
}
