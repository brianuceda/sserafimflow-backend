package com.brianuceda.sserafimflow.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.brianuceda.sserafimflow.entities.CurrencyRateEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRateDTO {
  private String currency;
  private BigDecimal purchasePrice;
  private BigDecimal salePrice;

  public CurrencyRateDTO(CurrencyRateEntity currencyRateEntity) {
    this.currency = currencyRateEntity.getCurrency();
    this.purchasePrice = currencyRateEntity.getPurchasePrice();
    this.salePrice = currencyRateEntity.getSalePrice();
  }
}