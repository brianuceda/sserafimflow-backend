package com.brianuceda.sserafimflow.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import com.brianuceda.sserafimflow.dtos.CurrencyRateDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "currency_rate")
public class CurrencyRateEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 3)
  private String currency; // USD

  @Column(precision = 6, scale = 3)
  @Positive
  private BigDecimal purchasePrice;

  @Column(precision = 6, scale = 3)
  @Positive
  private BigDecimal salePrice;

  // Relación con ExchangeRate
  @ManyToOne
  @JoinColumn(name = "exchange_rate_id")
  private ExchangeRateEntity exchangeRate;

  public CurrencyRateEntity(CurrencyRateDTO currencyRateDTO) {
    this.currency = currencyRateDTO.getCurrency();
    this.purchasePrice = currencyRateDTO.getPurchasePrice();
    this.salePrice = currencyRateDTO.getSalePrice();
  }
}
