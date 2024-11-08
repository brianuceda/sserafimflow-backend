package com.brianuceda.sserafimflow.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.brianuceda.sserafimflow.dtos.BankDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.brianuceda.sserafimflow.implementations._AuthBankImpl;
import com.brianuceda.sserafimflow.utils.DataUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;

@Log
@RestController
@RequestMapping("/api/v1/auth/bank")
public class _AuthBankController {
  @Value("${IS_PRODUCTION}")
  private boolean isProduction;

  private final _AuthBankImpl authBankImpl;

  public _AuthBankController(_AuthBankImpl authBankImpl) {
    this.authBankImpl = authBankImpl;
  }

  @PostMapping("/register")
  public ResponseEntity<ResponseDTO> register(HttpServletRequest request,
      @RequestPart(value = "dto", required = true) BankDTO bankDTO,
      @RequestPart(value = "rememberMe", required = false) Boolean rememberMe,
      @RequestPart(value = "image", required = false) MultipartFile image) {

    try {
      // Reset
      bankDTO.setId(null);

      // Validations
      this.validateOrigins(request);
      this.validationsRegisterBank(bankDTO);

      if (rememberMe == null) {
        rememberMe = false;
      }

      if (image != null) {
        DataUtils.isSupportedImage(image);
      }

      bankDTO.setNominalRate(bankDTO.getNominalRate().divide(BigDecimal.valueOf(100)));
      bankDTO.setEffectiveRate(bankDTO.getEffectiveRate().divide(BigDecimal.valueOf(100)));

      return new ResponseEntity<>(this.authBankImpl.register(bankDTO, image, rememberMe), HttpStatus.OK);
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
      @RequestPart(value = "dto", required = true) BankDTO bankDTO,
      @RequestPart(value = "rememberMe", required = false) Boolean rememberMe) {

    try {
      bankDTO.setId(null);
      this.validateOrigins(request);
      this.validationsLoginBank(bankDTO);

      if (rememberMe == null) {
        rememberMe = false;
      }

      return new ResponseEntity<>(this.authBankImpl.login(bankDTO, rememberMe), HttpStatus.OK);
    } catch (BadCredentialsException ex) {
      return new ResponseEntity<>(new ResponseDTO("Credenciales inválidos"), HttpStatus.UNAUTHORIZED);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    } catch (Exception ex) {
      log.severe("Error inesperado: " + ex.getMessage());
      return new ResponseEntity<>(new ResponseDTO("Ocurrió un error."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private void validationsRegisterBank(BankDTO bankDTO) {
    this.validationsLoginBank(bankDTO);

    if (bankDTO.getRealName() == null || bankDTO.getRealName().trim().isEmpty()) {
      throw new IllegalArgumentException("El nombre es obligatorio");
    }

    if (bankDTO.getRealName().length() > 255) {
      throw new IllegalArgumentException("El nombre es demasiado largo");
    }

    if (bankDTO.getRuc() == null || bankDTO.getRuc().trim().isEmpty()) {
      throw new IllegalArgumentException("El RUC es obligatorio");
    }

    if (bankDTO.getRuc().length() > 11) {
      throw new IllegalArgumentException("El RUC es demasiado largo");
    }

    if (bankDTO.getMainCurrency() == null) {
      throw new IllegalArgumentException("La moneda es obligatoria");
    }

    EnumSet<CurrencyEnum> currencies = EnumSet.of(CurrencyEnum.PEN, CurrencyEnum.USD);
    if (!currencies.contains(bankDTO.getMainCurrency())) {
      throw new IllegalArgumentException("La moneda no es válida");
    }

    validateRate(bankDTO.getNominalRate(), "tasa nominal");
    validateRate(bankDTO.getEffectiveRate(), "tasa efectiva");
  }

  private void validateRate(BigDecimal rate, String rateType) {
    if (rate == null) {
      throw new IllegalArgumentException("La " + rateType + " es obligatoria");
    }

    if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.valueOf(100)) > 0) {
      throw new IllegalArgumentException("La " + rateType + " debe estar entre 0 y 100");
    }
    
    rate = rate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
  }

  private void validationsLoginBank(BankDTO bankDTO) {
    if (bankDTO.getUsername() == null || bankDTO.getUsername().trim().isEmpty()) {
      throw new IllegalArgumentException("El correo es obligatorio");
    }

    if (bankDTO.getUsername().length() > 150) {
      throw new IllegalArgumentException("El correo es demasiado largo");
    }

    if (!DataUtils.isValidEmail(bankDTO.getUsername())) {
      throw new IllegalArgumentException("El correo no es válido");
    }

    if (bankDTO.getPassword() == null || bankDTO.getPassword().trim().isEmpty()) {
      throw new IllegalArgumentException("La contraseña es obligatoria");
    }

    if (bankDTO.getPassword().length() > 50) {
      throw new IllegalArgumentException("La contraseña es demasiado larga");
    }

    if (!DataUtils.isValidPassword(bankDTO.getPassword())) {
      throw new IllegalArgumentException("La contraseña no es válida");
    }
  }

  private void validateOrigins(HttpServletRequest request) {
    if (this.isProduction) {
      DataUtils.verifyAllowedOrigin(request);
    }
  }
}
