package com.brianuceda.sserafimflow.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import java.util.List;

import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exchange_rate")
public class ExchangeRateEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private LocalDate date; // yyyy-MM-dd

  @OneToMany(mappedBy = "exchangeRate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<CurrencyRateEntity> currencyRates;

  public ExchangeRateEntity(LocalDate date, List<CurrencyRateEntity> currencyRates) {
    this.date = date;
    this.currencyRates = currencyRates;
  }

  public ExchangeRateEntity(ExchangeRateDTO exchangeRateDTO) {
    this.date = exchangeRateDTO.getDate();
    this.currencyRates = exchangeRateDTO.getCurrencyRates().stream().map(CurrencyRateEntity::new).toList();
  }
}
