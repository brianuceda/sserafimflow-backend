package xyz.brianuceda.sserafimflow.implementations;

import xyz.brianuceda.sserafimflow.dtos.ExchangeRateDTO;

public interface ExchangeRateImpl {
  void setTodayExchangeRate();
  ExchangeRateDTO getTodayExchangeRate();
}
