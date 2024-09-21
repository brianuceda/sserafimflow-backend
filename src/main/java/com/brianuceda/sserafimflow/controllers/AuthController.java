package com.brianuceda.sserafimflow.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.exceptions.GeneralExceptions.ConnectionFailed;
import com.brianuceda.sserafimflow.implementations.ExchangeRateServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api")
public class AuthController {
  private final ExchangeRateServiceImpl exchangeRateServiceImpl;

  public AuthController(ExchangeRateServiceImpl exchangeRateServiceImpl) {
    this.exchangeRateServiceImpl = exchangeRateServiceImpl;
  }

  @GetMapping("today-exchange-rate-sbs")
  public ResponseEntity<?> getTodayExchangeRate() {
    try {
      ExchangeRateDTO exchangeRate = exchangeRateServiceImpl.getTodayExchangeRate();

      return new ResponseEntity<ExchangeRateDTO>(exchangeRate, HttpStatus.OK);
    } catch (ConnectionFailed e) {
      return new ResponseEntity<ResponseDTO>(new ResponseDTO(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}