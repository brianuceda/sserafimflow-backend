package com.brianuceda.sserafimflow.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.exceptions.GeneralExceptions.ConnectionFailed;
import com.brianuceda.sserafimflow.implementations.AuthServiceImpl;
import com.brianuceda.sserafimflow.implementations.ExchangeRateServiceImpl;
import com.brianuceda.sserafimflow.utils.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/v1/company")
public class CompanyController {
  private final JwtUtils jwtUtils;
  private final AuthServiceImpl authServiceImpl;
  private final ExchangeRateServiceImpl exchangeRateServiceImpl;

  public CompanyController(AuthServiceImpl authServiceImpl, ExchangeRateServiceImpl exchangeRateServiceImpl,
      JwtUtils jwtUtils) {
    this.authServiceImpl = authServiceImpl;
    this.exchangeRateServiceImpl = exchangeRateServiceImpl;
    this.jwtUtils = jwtUtils;
  }

  @PostMapping("logout")
  public ResponseEntity<ResponseDTO> logout(HttpServletRequest request) {
    try {
      var token = jwtUtils.getTokenFromRequest(request);

      return new ResponseEntity<>(authServiceImpl.logout(token), HttpStatus.OK);
    } catch (BadCredentialsException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("today-exchange-rate-sbs")
  public ResponseEntity<?> getTodayExchangeRate() {
    try {
      return new ResponseEntity<ExchangeRateDTO>(exchangeRateServiceImpl.getTodayExchangeRate(), HttpStatus.OK);
    } catch (ConnectionFailed e) {
      return new ResponseEntity<ResponseDTO>(new ResponseDTO(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

}
