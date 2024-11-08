package com.brianuceda.sserafimflow.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.brianuceda.sserafimflow.dtos.CompanyDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.brianuceda.sserafimflow.implementations._AuthCompanyImpl;
import com.brianuceda.sserafimflow.utils.DataUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;

import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;

@Log
@RestController
@RequestMapping("/api/v1/auth/company")
public class _AuthCompanyController {
  @Value("${IS_PRODUCTION}")
  private boolean isProduction;

  private final _AuthCompanyImpl authCompanyImpl;

  public _AuthCompanyController(_AuthCompanyImpl authCompanyImpl) {
    this.authCompanyImpl = authCompanyImpl;
  }

  @PostMapping("/register")
  public ResponseEntity<ResponseDTO> register(HttpServletRequest request,
      @RequestPart(value = "dto", required = true) CompanyDTO companyDTO,
      @RequestPart(value = "rememberMe", required = false) Boolean rememberMe,
      @RequestPart(value = "image", required = false) MultipartFile image) {

    try {
      // Reset
      companyDTO.setId(null);

      // Validations
      this.validateOrigins(request);
      this.validationsRegisterCompany(companyDTO);

      if (rememberMe == null) {
        rememberMe = false;
      }

      if (image != null) {
        DataUtils.isSupportedImage(image);
      }

      return new ResponseEntity<>(this.authCompanyImpl.register(companyDTO, image, rememberMe), HttpStatus.OK);
    } catch (BadCredentialsException ex) {
      return new ResponseEntity<>(new ResponseDTO("Credenciales inválidos"), HttpStatus.UNAUTHORIZED);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    } catch (Exception ex) {
      log.severe("Error inesperado: " + ex.getMessage());
      return new ResponseEntity<>(new ResponseDTO("Ocurrió un error."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/login")
  public ResponseEntity<ResponseDTO> login(HttpServletRequest request,
      @RequestPart(value = "dto", required = true) CompanyDTO companyDTO,
      @RequestPart(value = "rememberMe", required = false) Boolean rememberMe) {

    try {
      this.validateOrigins(request);
      this.validationsLoginCompany(companyDTO);

      if (rememberMe == null) {
        rememberMe = false;
      }

      return new ResponseEntity<>(this.authCompanyImpl.login(companyDTO, rememberMe), HttpStatus.OK);
    } catch (BadCredentialsException ex) {
      return new ResponseEntity<>(new ResponseDTO("Credenciales inválidos"), HttpStatus.UNAUTHORIZED);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    } catch (Exception ex) {
      log.severe("Error inesperado: " + ex.getMessage());
      return new ResponseEntity<>(new ResponseDTO("Ocurrió un error."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private void validationsRegisterCompany(CompanyDTO companyDTO) {
    this.validationsLoginCompany(companyDTO);

    if (companyDTO.getRealName() == null || companyDTO.getRealName().trim().isEmpty()) {
      throw new IllegalArgumentException("El nombre es obligatorio");
    }

    if (companyDTO.getRealName().length() > 255) {
      throw new IllegalArgumentException("El nombre es demasiado largo");
    }

    if (companyDTO.getRuc() == null || companyDTO.getRuc().trim().isEmpty()) {
      throw new IllegalArgumentException("El RUC es obligatorio");
    }

    if (companyDTO.getRuc().length() > 11) {
      throw new IllegalArgumentException("El RUC es demasiado largo");
    }

    if (companyDTO.getMainCurrency() == null) {
      throw new IllegalArgumentException("La moneda es obligatoria");
    }

    EnumSet<CurrencyEnum> currencies = EnumSet.of(CurrencyEnum.PEN, CurrencyEnum.USD);
    if (!currencies.contains(companyDTO.getMainCurrency())) {
      throw new IllegalArgumentException("La moneda no es válida");
    }
  }

  private void validationsLoginCompany(CompanyDTO companyDTO) {
    if (companyDTO.getUsername() == null || companyDTO.getUsername().trim().isEmpty()) {
      throw new IllegalArgumentException("El correo es obligatorio");
    }

    if (companyDTO.getUsername().length() > 150) {
      throw new IllegalArgumentException("El correo es demasiado largo");
    }

    if (!DataUtils.isValidEmail(companyDTO.getUsername())) {
      throw new IllegalArgumentException("El correo no es válido");
    }

    if (companyDTO.getPassword() == null || companyDTO.getPassword().trim().isEmpty()) {
      throw new IllegalArgumentException("La contraseña es obligatoria");
    }

    if (companyDTO.getPassword().length() > 50) {
      throw new IllegalArgumentException("La contraseña es demasiado larga");
    }

    if (!DataUtils.isValidPassword(companyDTO.getPassword())) {
      throw new IllegalArgumentException("La contraseña no es válida");
    }
  }

  private void validateOrigins(HttpServletRequest request) {
    if (this.isProduction) {
      DataUtils.verifyAllowedOrigin(request);
    }
  }
}
