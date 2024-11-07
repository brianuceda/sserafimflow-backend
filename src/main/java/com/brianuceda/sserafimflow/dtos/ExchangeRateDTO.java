package com.brianuceda.sserafimflow.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;

import java.time.LocalDate;

import com.brianuceda.sserafimflow.entities.ExchangeRateEntity;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExchangeRateDTO {
  private LocalDate date;
  private List<CurrencyRateDTO> currencyRates;

  public ExchangeRateDTO() {
    this.currencyRates = new ArrayList<>();
  }

  public ExchangeRateDTO(ExchangeRateEntity currencyRates) {
    this.date = currencyRates.getDate();
    this.currencyRates = currencyRates.getCurrencyRates().stream().map(CurrencyRateDTO::new).toList();
  }

  public void assingCurrencyNames() {
    Map<CurrencyEnum, String> currencyNames = Map.of(
        CurrencyEnum.USD, "Dólar Americano",
        CurrencyEnum.CAD, "Dólar Canadiense",
        CurrencyEnum.EUR, "Euro");

    this.getCurrencyRates().forEach(currencyRate -> {
      currencyRate.setCurrencyName(currencyNames.get(currencyRate.getCurrency()));
    });
  }

}
