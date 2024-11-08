package com.brianuceda.sserafimflow.controllers;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brianuceda.sserafimflow.dtos.BankDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.implementations._AuthBankImpl;
import com.brianuceda.sserafimflow.implementations.BankImpl;
import com.brianuceda.sserafimflow.utils.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;

@RestController
@RequestMapping("/api/v1/bank")
@Log
public class BankController {
  private final JwtUtils jwtUtils;
  private final _AuthBankImpl authBankImpl;
  private final BankImpl bankImpl;

  public BankController(
      _AuthBankImpl authBankImpl,
      JwtUtils jwtUtils,
      BankImpl bankImpl) {
    
    this.authBankImpl = authBankImpl;
    this.jwtUtils = jwtUtils;
    this.bankImpl = bankImpl;
  }

  @PreAuthorize("hasRole('BANK')")
  @PostMapping("/logout")
  public ResponseEntity<ResponseDTO> logout(HttpServletRequest request) {
    try {
      var token = jwtUtils.getTokenFromRequest(request);
      return new ResponseEntity<>(authBankImpl.logout(token), HttpStatus.OK);
    } catch (BadCredentialsException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }
  
  @PreAuthorize("hasRole('BANK')")
  @GetMapping("/profile")
  public ResponseEntity<?> getProfile(HttpServletRequest request, BigDecimal amount) {
    try {
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);

      BankDTO bank = bankImpl.getProfile(username);

      log.info("Bank profile: " + bank.toString());

      return new ResponseEntity<>(bank, HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @PreAuthorize("hasRole('BANK')")
  @PostMapping("/add-money")
  public ResponseEntity<?> addMoney(HttpServletRequest request, BigDecimal amount) {
    try {
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);

      return new ResponseEntity<>(bankImpl.addMoney(username, amount), HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }
  
  @PreAuthorize("hasAnyRole('BANK', 'COMPANY')")
  @GetMapping("/all-banks-associated")
  public ResponseEntity<?> getAllBanks(HttpServletRequest request) {
    try {
      return new ResponseEntity<>(bankImpl.getAllBanks(), HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @PreAuthorize("hasRole('BANK')")
  @GetMapping("/dashboard")
  public ResponseEntity<ResponseDTO> accessBankDashboard() {
    return new ResponseEntity<>(new ResponseDTO("Access granted to Bank Dashboard!"), HttpStatus.OK);
  }

}
