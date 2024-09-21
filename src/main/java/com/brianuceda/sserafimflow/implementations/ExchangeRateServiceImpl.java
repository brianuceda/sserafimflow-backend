package com.brianuceda.sserafimflow.implementations;

import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;
import java.time.LocalDate;

public interface ExchangeRateServiceImpl {
  ExchangeRateDTO getTodayExchangeRate(LocalDate currentDate);
}
