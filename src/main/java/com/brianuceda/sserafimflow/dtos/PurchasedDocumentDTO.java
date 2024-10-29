package com.brianuceda.sserafimflow.dtos;

import com.brianuceda.sserafimflow.entities.PurchaseEntity;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.brianuceda.sserafimflow.enums.StateEnum;
import com.brianuceda.sserafimflow.enums.RateTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchasedDocumentDTO {
  private Long id;

  private LocalDate purchaseDate;
  private LocalDateTime payDate;

  private CurrencyEnum currency;
  private BigDecimal nominalValue;
  private BigDecimal discountRate;
  private BigDecimal receivedValue;
  private Integer days;
  private BigDecimal tep;
  private RateTypeEnum rateType;
  private BigDecimal rateValue;
  private StateEnum state;

  private BankDTO bank;
  private CompanyDTO company;

  private DocumentDTO document;

  public PurchasedDocumentDTO(PurchaseEntity purchase) {
    this.id = purchase.getId();
    this.purchaseDate = purchase.getPurchaseDate();
    this.payDate = purchase.getPayDate();
    this.currency = purchase.getCurrency();
    this.nominalValue = purchase.getNominalValue();
    this.discountRate = purchase.getDiscountRate();
    this.receivedValue = purchase.getReceivedValue();
    this.days = purchase.getDays();
    this.tep = purchase.getTep();
    this.rateType = purchase.getRateType();
    this.rateValue = purchase.getRateValue();
    this.state = purchase.getState();

    this.bank = new BankDTO(
        purchase.getBank().getId(),
        purchase.getBank().getRealName(),
        purchase.getBank().getRuc(),
        purchase.getBank().getCurrency());

    this.company = new CompanyDTO(
        purchase.getDocument().getCompany().getRealName(),
        purchase.getDocument().getCompany().getRuc(),
        purchase.getDocument().getCompany().getImageUrl());

    this.document = new DocumentDTO(
        purchase.getDocument().getId(),
        purchase.getDocument().getDocumentType(),
        purchase.getDocument().getDueDate(),
        purchase.getDocument().getClientName());
  }
}
