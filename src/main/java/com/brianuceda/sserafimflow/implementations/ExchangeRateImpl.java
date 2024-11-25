package com.brianuceda.sserafimflow.implementations;

import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;

public interface ExchangeRateImpl {
  void setTodayExchangeRate();
  ExchangeRateDTO getTodayExchangeRate();
}
