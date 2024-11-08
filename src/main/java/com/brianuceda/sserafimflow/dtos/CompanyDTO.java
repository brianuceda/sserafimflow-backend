package com.brianuceda.sserafimflow.dtos;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

import com.brianuceda.sserafimflow.entities.CompanyEntity;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyDTO {
  private Long id;
  private String realName;
  private String ruc;
  private String username;
  private String password;
  private String imageUrl;
  private CurrencyEnum mainCurrency;
  private CurrencyEnum previewDataCurrency;
  private BigDecimal balance;
  private LocalDate creationDate;
  private Timestamp accountCreationDate;

  public CompanyDTO(CompanyEntity company, boolean includeId) {
    if (includeId) {
      this.id = company.getId();
    } 
    this.realName = company.getRealName();
    this.ruc = company.getRuc();
    this.username = company.getUsername();
    this.imageUrl = company.getImageUrl();
    this.mainCurrency = company.getMainCurrency();
    this.previewDataCurrency = company.getPreviewDataCurrency();
    this.balance = company.getBalance();
    this.creationDate = company.getCreationDate();
    this.accountCreationDate = company.getAccountCreationDate();
  }

  // Para mostrar en la compra
  public CompanyDTO(String realName, String ruc, String imageUrl) {
    this.realName = realName;
    this.ruc = ruc;
    this.imageUrl = imageUrl;
  }
}
