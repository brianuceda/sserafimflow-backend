package com.brianuceda.sserafimflow.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brianuceda.sserafimflow.dtos.BankDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.implementations._AuthBankImpl;
import com.brianuceda.sserafimflow.utils.DataUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Log
@RestController
@RequestMapping("/api/v1/auth/bank")
public class _AuthBankController {
  @Value("${IS_PRODUCTION}")
  private Boolean isProduction;
  @Value("${FRONTEND_URL1}")
  private String frontendUrl1;
  @Value("${FRONTEND_URL2}")
  private String frontendUrl2;  
  
  private List<String> allowedOrigins;
  private final _AuthBankImpl authBankImpl;

  public _AuthBankController(_AuthBankImpl authBankImpl) {
    this.authBankImpl = authBankImpl;
  }

  @PostConstruct
  private void init() {
    this.allowedOrigins = List.of(frontendUrl1, frontendUrl2);
  }

  @PostMapping("/register")
  public ResponseEntity<ResponseDTO> register(HttpServletRequest request, @RequestBody BankDTO bankDTO) {
    try {
      bankDTO.setId(null);
      return new ResponseEntity<>(this.authBankImpl.register(bankDTO), HttpStatus.OK);
    } catch (BadCredentialsException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }
  }

  @PostMapping("/login")
  public ResponseEntity<ResponseDTO> login(HttpServletRequest request, @RequestBody BankDTO bankDTO) {
    try {
      bankDTO.setId(null);
      return new ResponseEntity<>(this.authBankImpl.login(bankDTO), HttpStatus.OK);
    } catch (BadCredentialsException ex) {
      return new ResponseEntity<>(new ResponseDTO("Credenciales inválidas"), HttpStatus.UNAUTHORIZED);
    }
  }

  // private void v
}
