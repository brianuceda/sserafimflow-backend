package com.brianuceda.sserafimflow.controllers;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.implementations.CompanyImpl;
import com.brianuceda.sserafimflow.implementations._AuthCompanyImpl;
import com.brianuceda.sserafimflow.utils.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/v1/company")
public class CompanyController {
  private final JwtUtils jwtUtils;
  private final _AuthCompanyImpl authCompanyImpl;
  private final CompanyImpl companyImpl;

  public CompanyController(
      _AuthCompanyImpl authCompanyImpl,
      JwtUtils jwtUtils,
      CompanyImpl companyImpl) {
    
    this.authCompanyImpl = authCompanyImpl;
    this.jwtUtils = jwtUtils;
    this.companyImpl = companyImpl;
  }

  @PreAuthorize("hasRole('COMPANY')")
  @PostMapping("/logout")
  public ResponseEntity<ResponseDTO> logout(HttpServletRequest request) {
    try {
      var token = jwtUtils.getTokenFromRequest(request);
      return new ResponseEntity<>(authCompanyImpl.logout(token), HttpStatus.OK);
    } catch (BadCredentialsException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }
  
  @PreAuthorize("hasRole('COMPANY')")
  @GetMapping("/profile")
  public ResponseEntity<?> getProfile(HttpServletRequest request, BigDecimal amount) {
    try {
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);

      return new ResponseEntity<>(companyImpl.getProfile(username), HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @PreAuthorize("hasRole('COMPANY')")
  @GetMapping("/dashboard")
  public ResponseEntity<ResponseDTO> accessCompanyDashboard() {
    return new ResponseEntity<>(new ResponseDTO("Access granted to Company Dashboard!"), HttpStatus.OK);
  }
}
