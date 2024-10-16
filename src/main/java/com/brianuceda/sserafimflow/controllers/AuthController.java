package com.brianuceda.sserafimflow.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brianuceda.sserafimflow.dtos.CompanyDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.implementations.AuthServiceImpl;
import com.brianuceda.sserafimflow.utils.DataUtils;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

// import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {"http://localhost:4200", "https://sserafimflow.vercel.app"})
public class AuthController {
  @Value("${FRONTEND_URL1}")
  private String frontendUrl1;
  @Value("${FRONTEND_URL2}")
  private String frontendUrl2;

  @SuppressWarnings("unused")
  private List<String> allowedOrigins;

  private final AuthServiceImpl authServiceImpl;

  public AuthController(AuthServiceImpl authServiceImpl) {
    this.authServiceImpl = authServiceImpl;
  }

  @PostConstruct
  private void init() {
    this.allowedOrigins = List.of(frontendUrl1, frontendUrl2);
  }

  @PostMapping("signup")
  public ResponseEntity<ResponseDTO> register(HttpServletRequest request, @RequestBody CompanyDTO companyDTO) {
    try {
      this.validationsAuth(request, companyDTO);

      return new ResponseEntity<>(this.authServiceImpl.signup(companyDTO), HttpStatus.OK);
    } catch (BadCredentialsException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }
  }

  @PostMapping("signin")
  public ResponseEntity<ResponseDTO> login(HttpServletRequest request, @RequestBody CompanyDTO companyDTO) {
    try {
      this.validationsAuth(request, companyDTO);

      return new ResponseEntity<>(this.authServiceImpl.signin(companyDTO), HttpStatus.OK);
    } catch (BadCredentialsException ex) {
      return new ResponseEntity<>(new ResponseDTO("Credenciales inválidas"), HttpStatus.UNAUTHORIZED);
    }
  }

  private void validationsAuth(HttpServletRequest request, CompanyDTO companyDTO) {
    // DataUtils.verifyAllowedOrigin(request, this.allowedOrigins);
    DataUtils.verifySQLInjection(companyDTO.getEmail());
    DataUtils.verifySQLInjection(companyDTO.getPassword());
  }
}