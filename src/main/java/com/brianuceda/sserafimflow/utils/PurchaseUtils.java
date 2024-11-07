package com.brianuceda.sserafimflow.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.brianuceda.sserafimflow.dtos.CurrencyRateDTO;
import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.brianuceda.sserafimflow.services.ExchangeRateService;

@Component
public class PurchaseUtils {
  @Value("${IS_PRODUCTION}")
  private boolean isProduction;

  private final ExchangeRateService exchangeRateService;

  public PurchaseUtils(ExchangeRateService exchangeRateService) {
    this.exchangeRateService = exchangeRateService;
  }

  public ExchangeRateDTO getTodayExchangeRate() {
    return this.exchangeRateService.getTodayExchangeRate();
  }

  public BigDecimal convertCurrency(BigDecimal amount, CurrencyEnum fromCurrency, CurrencyEnum toCurrency,
      ExchangeRateDTO exchangeRateDTO) {
    
    if (fromCurrency == toCurrency) {
      return amount; // No necesita conversión
    }

    Optional<CurrencyRateDTO> rate = exchangeRateDTO.getCurrencyRates().stream()
        .filter(r -> r.getCurrency() == CurrencyEnum.USD)
        .findFirst();

    if (rate.isEmpty()) {
      throw new IllegalStateException("No se encontró la tasa de cambio para USD en el ExchangeRateDTO");
    }

    if (fromCurrency == CurrencyEnum.USD && toCurrency == CurrencyEnum.PEN) {
      // USD -> PEN: PurchasePrice
      return amount.multiply(rate.get().getPurchasePrice());
    } else if (fromCurrency == CurrencyEnum.PEN && toCurrency == CurrencyEnum.USD) {
      // PEN -> USD: SalePrice
      return amount.divide(rate.get().getSalePrice(), RoundingMode.HALF_UP);
    }

    return amount;
  }
}
