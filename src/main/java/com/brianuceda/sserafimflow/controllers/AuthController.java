package com.brianuceda.sserafimflow.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brianuceda.sserafimflow.dtos.CompanyDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.implementations.AuthServiceImpl;
import com.brianuceda.sserafimflow.utils.DataUtils;

import jakarta.servlet.http.HttpServletRequest;

// import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  @Value("${FRONTEND_URL}")
  private String frontendUrl;

  private final AuthServiceImpl authServiceImpl;

  public AuthController(AuthServiceImpl authServiceImpl) {
    this.authServiceImpl = authServiceImpl;
  }

  @PostMapping("register")
  public ResponseEntity<ResponseDTO> register(HttpServletRequest request, @RequestBody CompanyDTO companyDTO) {
    try {
      this.validationsAuth(request, companyDTO);

      return new ResponseEntity<>(this.authServiceImpl.register(companyDTO), HttpStatus.OK);
    } catch (BadCredentialsException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }
  }

  @PostMapping("login")
  public ResponseEntity<ResponseDTO> login(HttpServletRequest request, @RequestBody CompanyDTO companyDTO) {
    try {
      this.validationsAuth(request, companyDTO);

      return new ResponseEntity<>(this.authServiceImpl.login(companyDTO), HttpStatus.OK);
    } catch (BadCredentialsException ex) {
      return new ResponseEntity<>(new ResponseDTO("Credenciales incorrectas"), HttpStatus.UNAUTHORIZED);
    }
  }

  private void validationsAuth(HttpServletRequest request, CompanyDTO companyDTO) {
    // DataUtils.verifyAllowedOrigin(request, Arrays.asList(this.frontendUrl));
    DataUtils.verifySQLInjection(companyDTO.getEmail());
    DataUtils.verifySQLInjection(companyDTO.getPassword());
  }
}