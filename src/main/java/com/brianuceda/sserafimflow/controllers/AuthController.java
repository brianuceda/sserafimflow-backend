package com.brianuceda.sserafimflow.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.exceptions.GeneralExceptions.ConnectionFailed;
import com.brianuceda.sserafimflow.implementations.ExchangeRateServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class AuthController {
  @Value("${IS_PRODUCTION}")
  private boolean isProduction;

  private ExchangeRateServiceImpl exchangeRateServiceImpl;

  public AuthController(ExchangeRateServiceImpl exchangeRateServiceImpl) {
    this.exchangeRateServiceImpl = exchangeRateServiceImpl;
  }

  @GetMapping("today-exchange-rate-sbs")
  public ResponseEntity<?> getTodayExchangeRate() {
    try {
      // Fecha actual (Prod: UTC / Dev: GMT-5)
      LocalDate currentDate = this.isProduction ? LocalDateTime.now().minusHours(5).toLocalDate() : LocalDate.now();

      ExchangeRateDTO exchangeRate = exchangeRateServiceImpl.getTodayExchangeRate(currentDate);

      return new ResponseEntity<ExchangeRateDTO>(exchangeRate, HttpStatus.OK);
    } catch (ConnectionFailed e) {
      return new ResponseEntity<ResponseDTO>(new ResponseDTO(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}