package com.brianuceda.sserafimflow.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.brianuceda.sserafimflow.entities.CurrencyRateEntity;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrencyRateDTO {
  private String currencyName;
  private CurrencyEnum currency;
  private BigDecimal purchasePrice;
  private BigDecimal salePrice;

  public CurrencyRateDTO(CurrencyRateEntity currencyRateEntity) {
    this.currency = currencyRateEntity.getCurrency();
    this.purchasePrice = currencyRateEntity.getPurchasePrice();
    this.salePrice = currencyRateEntity.getSalePrice();
  }

  public CurrencyRateDTO(CurrencyEnum currency, BigDecimal purchasePrice, BigDecimal salePrice) {
    this.currency = currency;
    this.purchasePrice = purchasePrice;
    this.salePrice = salePrice;
  }
}