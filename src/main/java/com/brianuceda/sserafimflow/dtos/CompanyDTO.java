package com.brianuceda.sserafimflow.dtos;

import java.math.BigDecimal;
import java.sql.Timestamp;

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
  private String realName;
  private String ruc;
  private String username;
  private String password;
  private String imageUrl;
  private CurrencyEnum currency;
  private BigDecimal balance;
  private Timestamp creationDate;

  public CompanyDTO(CompanyEntity company) {
    this.realName = company.getRealName();
    this.ruc = company.getRuc();
    this.username = company.getUsername();
    this.imageUrl = company.getImageUrl();
    this.currency = company.getCurrency();
    this.balance = company.getBalance();
    this.creationDate = company.getCreationDate();
  }

  // Para mostrar en la compra
  public CompanyDTO(String realName, String ruc, String imageUrl) {
    this.realName = realName;
    this.ruc = ruc;
    this.imageUrl = imageUrl;
  }
}
