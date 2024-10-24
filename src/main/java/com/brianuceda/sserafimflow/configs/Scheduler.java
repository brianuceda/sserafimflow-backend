package com.brianuceda.sserafimflow.configs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.brianuceda.sserafimflow.implementations.ExchangeRateImpl;

import lombok.extern.java.Log;

@Component
@Log
public class Scheduler {
  @Value("${IS_PRODUCTION}")
  private Boolean isProduction;

  private final ExchangeRateImpl exchangeRateImpl;

  public Scheduler(ExchangeRateImpl exchangeRateImpl) {
    this.exchangeRateImpl = exchangeRateImpl;
  }

  // Tareas automatizadas
  // 1 minuto: 60000
  // 1 hora: 3600000
  @Scheduled(fixedRate = 3600000)
  private void autoSaveTodayExchangeRates() {
    LocalDateTime now = LocalDateTime.now();
    int[] range = { 1, 3 };

    // Si la fecha actual está entre las 1:00 am y las 3:00 am
    if (now.getHour() >= range[0] && now.getHour() < range[1]) {
      // formato dd-mm-yyyy
      log.info("Guardando el tipo de cambio del dia " + now.toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
      this.exchangeRateImpl.getTodayExchangeRate();
    } else {
      String message = "La hora actual es " + now.getHour() + " " + (now.getHour() < 12 ? "am" : "pm") + " y no se encuentra dentro del rango de ";
      message += "[" + range[0] + " a " + range[1] + "] " + (range[1] < 12 ? "am" : "pm");
      log.info(message);
    }
  }
}