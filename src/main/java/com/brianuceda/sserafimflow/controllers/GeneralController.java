package com.brianuceda.sserafimflow.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.exceptions.GeneralExceptions.ConnectionFailed;
import com.brianuceda.sserafimflow.implementations.ExchangeRateImpl;

@RestController
@RequestMapping("/api/v1/general")
public class GeneralController {
  private final ExchangeRateImpl exchangeRateImpl;

  public GeneralController(ExchangeRateImpl exchangeRateImpl) {
    this.exchangeRateImpl = exchangeRateImpl;
  }

  @PreAuthorize("hasAnyRole('COMPANY', 'BANK')")
  @GetMapping("/today-exchange-rate-sbs")
  public ResponseEntity<?> getTodayExchangeRate() {
    try {
      return new ResponseEntity<ExchangeRateDTO>(exchangeRateImpl.getTodayExchangeRate(), HttpStatus.OK);
    } catch (ConnectionFailed e) {
      return new ResponseEntity<ResponseDTO>(new ResponseDTO(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PreAuthorize("hasAnyRole('COMPANY', 'BANK')")
  @GetMapping("/is-valid-token")
  public ResponseEntity<?> isValidToken() {
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
