package com.brianuceda.sserafimflow.dtos;

import com.brianuceda.sserafimflow.dtos.portfolio.PortfolioDTO;
import com.brianuceda.sserafimflow.entities.DocumentEntity;
import com.brianuceda.sserafimflow.entities.PortfolioEntity;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.brianuceda.sserafimflow.enums.StateEnum;
import com.brianuceda.sserafimflow.enums.DocumentTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentDTO {
  private Long id;
  private DocumentTypeEnum documentType;
  private BigDecimal amount;
  private CurrencyEnum currency;
  private LocalDate issueDate;
  private LocalDate discountDate;
  private LocalDate expirationDate;
  private StateEnum state;
  private String clientName;
  private String clientPhone;

  private BankDTO bank;
  private PortfolioDTO portfolio;

  // Documentos según estado
  public DocumentDTO(DocumentEntity document) {
    this.id = document.getId();
    this.documentType = document.getDocumentType();
    this.amount = document.getAmount();
    this.currency = document.getCurrency();
    this.issueDate = document.getIssueDate();
    this.discountDate = document.getDiscountDate();
    this.expirationDate = document.getExpirationDate();
    this.state = document.getState();
    this.clientName = document.getClientName();
    this.clientPhone = document.getClientPhone();
  }

  // Para mostrar en la compra y en la cartera
  public DocumentDTO(Long id, DocumentTypeEnum documentType, String clientName) {
    this.id = id;
    this.documentType = documentType;
    this.clientName = clientName;
  }

  public void addPortfolioIfExists(PortfolioEntity portfolio) {
    this.setPortfolio(portfolio != null ? new PortfolioDTO(portfolio) : null);
  }
}
