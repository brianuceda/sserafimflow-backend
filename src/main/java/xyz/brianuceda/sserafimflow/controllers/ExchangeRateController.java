package xyz.brianuceda.sserafimflow.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import xyz.brianuceda.sserafimflow.dtos.ExchangeRateDTO;
import xyz.brianuceda.sserafimflow.implementations.ExchangeRateImpl;

@RestController
@RequestMapping("/api/v1/exchange-rate")
public class ExchangeRateController {
  private final ExchangeRateImpl exchangeRateImpl;

  public ExchangeRateController(ExchangeRateImpl exchangeRateImpl) {
    this.exchangeRateImpl = exchangeRateImpl;
  }
  
  @GetMapping("/today")
  public ResponseEntity<ExchangeRateDTO> getTodayExchangeRate() {
    return new ResponseEntity<>(exchangeRateImpl.getTodayExchangeRate(), HttpStatus.OK);
  }
}
