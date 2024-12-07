package com.brianuceda.sserafimflow.services;

import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.brianuceda.sserafimflow.dtos.CurrencyRateDTO;
import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;
import com.brianuceda.sserafimflow.entities.CurrencyRateEntity;
import com.brianuceda.sserafimflow.entities.ExchangeRateEntity;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.brianuceda.sserafimflow.exceptions.GeneralExceptions.ConnectionFailed;
import com.brianuceda.sserafimflow.implementations.ExchangeRateImpl;
import com.brianuceda.sserafimflow.respositories.ExchangeRateRepository;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;

@Service
public class ExchangeRateService implements ExchangeRateImpl {

  private final ExchangeRateRepository exchangeRateRepository;

  public ExchangeRateService(ExchangeRateRepository exchangeRateRepository) {
    this.exchangeRateRepository = exchangeRateRepository;
  }

  @Override
  @Transactional
  public void setTodayExchangeRate() {
    ExchangeRateEntity exchangeRateEntity = exchangeRateRepository.findById(1L).orElse(null);
    exchangeRateEntity.setDate(LocalDate.now());

    this.exchangeRateRepository.save(exchangeRateEntity);
  }

  @Override
  @Transactional
  public ExchangeRateDTO getTodayExchangeRate() throws ConnectionFailed {
    LocalDate currentDate = LocalDate.now();

    ExchangeRateEntity exchangeRateEntity = exchangeRateRepository.findByDate(currentDate);
    ExchangeRateDTO exchangeRateDTO = null;

    // Si no existe en la BD
    if (exchangeRateEntity == null) {
      // Obtener de la API de Magin Loops
      exchangeRateDTO = this.getExchangeRateFromMaginLoopsAPI(currentDate);

      // Asignar relaciones
      exchangeRateEntity = new ExchangeRateEntity(exchangeRateDTO);
      for (CurrencyRateEntity currencyRateEntity : exchangeRateEntity.getCurrencyRates()) {
        currencyRateEntity.setExchangeRate(exchangeRateEntity);
      }

      // Guardar en la BD
      exchangeRateRepository.save(exchangeRateEntity);
    } else {
      // Si existe en la BD, convertir a DTO para retornar
      exchangeRateDTO = new ExchangeRateDTO(exchangeRateEntity);
      exchangeRateDTO.assingCurrencyNames();
    }

    return exchangeRateDTO;
  }

  private ExchangeRateDTO getExchangeRateFromMaginLoopsAPI(LocalDate currentDate) {
    String apiUrl = "https://magicloops.dev/api/loop/68aa0b17-0dea-4ec8-8a13-d31e80d65ff8/run?input=I+love+Magic+Loops%21";

    try {
      // Crear una instancia de RestTemplate para llamar a la API
      RestTemplate restTemplate = new RestTemplate();

      // Especificar el tipo genérico esperado para la respuesta
      ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {
      };

      // Llamar a la API y obtener la respuesta
      ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
          apiUrl, HttpMethod.GET, null, responseType);
      Map<String, Object> responseBody = responseEntity.getBody();

      // Extraer y mapear los datos de la respuesta
      ExchangeRateDTO exchangeRateDTO = new ExchangeRateDTO();
      exchangeRateDTO.setDate(currentDate);

      if (responseBody == null || !responseBody.containsKey("currencyRates")) {
        throw new ConnectionFailed("Invalid response from Magin Loops API");
      }

      Object currencyRatesObj = responseBody.get("currencyRates");

      if (!(currencyRatesObj instanceof List<?>)) {
        throw new ConnectionFailed("Unexpected format for currencyRates in API response");
      }

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> currencyRates = (List<Map<String, Object>>) currencyRatesObj;

      for (Map<String, Object> rate : currencyRates) {
        String currencyCode = (String) rate.get("currency");
        CurrencyEnum currencyEnum = CurrencyEnum.valueOf(currencyCode);
        BigDecimal purchasePrice = new BigDecimal((String) rate.get("purchasePrice"));
        BigDecimal salePrice = new BigDecimal((String) rate.get("salePrice"));
        String currencyName = (String) rate.get("currencyName");

        CurrencyRateDTO currencyRateDTO = new CurrencyRateDTO(currencyName, currencyEnum, purchasePrice, salePrice);
        exchangeRateDTO.getCurrencyRates().add(currencyRateDTO);
      }

      return exchangeRateDTO;

    } catch (Exception ex) {
      throw new ConnectionFailed("Error fetching data from Magin Loops API: " + ex.getMessage());
    }
  }
}
