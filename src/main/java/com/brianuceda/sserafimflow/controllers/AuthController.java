package com.brianuceda.sserafimflow.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.exceptions.DataExceptions.InvalidDate;
import com.brianuceda.sserafimflow.exceptions.GeneralExceptions.ConnectionFailed;
import com.brianuceda.sserafimflow.implementations.ExchangeRateServiceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class AuthController {
  @Value("${IS_PRODUCTION}")
  private boolean isProduction;

  private ExchangeRateServiceImpl exchangeRateServiceImpl;

  public AuthController(ExchangeRateServiceImpl exchangeRateServiceImpl) {
    this.exchangeRateServiceImpl = exchangeRateServiceImpl;
  }

  @GetMapping("tasa-de-cambio-sunat")
  public ResponseEntity<?> getExchangeRate(@RequestParam LocalDate fecha) { // yyyy-MM-dd
    try {
      fecha = validationsDate(fecha);

      ExchangeRateDTO exchangeRate = exchangeRateServiceImpl.getExchangeRateApi(fecha);
      return new ResponseEntity<ExchangeRateDTO>(exchangeRate, HttpStatus.OK);
    } catch (ConnectionFailed | InvalidDate e) {
      return new ResponseEntity<ResponseDTO>(new ResponseDTO(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private LocalDate validationsDate(LocalDate fecha) throws InvalidDate {
    // En producción la hora es UTC, en desarrollo la hora es GMT-5 (Perú)
    LocalDate currentDate = LocalDate.now();
    LocalTime currentTime = LocalTime.now();

    if (isProduction) {
      currentDate = LocalDateTime.now().minusHours(5).toLocalDate();
      currentTime = LocalDateTime.now().minusHours(5).toLocalTime();
    }

    // La fecha no puede ser futura
    if (fecha.isAfter(currentDate)) {
      fecha = currentDate;
      // throw new ConnectionFailed("No se puede obtener la tasa de cambio de una fecha futura");
    }

    // Si la fecha es hoy y la hora es antes de las 6 AM, usar la fecha del día anterior
    int minHourOfDay = 6;
    if (fecha.isEqual(currentDate) && currentTime.isBefore(LocalTime.of(minHourOfDay, 0))) {
      fecha = fecha.minusDays(1);
    }

    return fecha;
  }
}