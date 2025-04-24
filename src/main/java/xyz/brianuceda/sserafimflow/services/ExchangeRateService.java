package xyz.brianuceda.sserafimflow.services;

import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import xyz.brianuceda.sserafimflow.dtos.CurrencyRateDTO;
import xyz.brianuceda.sserafimflow.dtos.ExchangeRateDTO;
import xyz.brianuceda.sserafimflow.entities.CurrencyRateEntity;
import xyz.brianuceda.sserafimflow.entities.ExchangeRateEntity;
import xyz.brianuceda.sserafimflow.enums.CurrencyEnum;
import xyz.brianuceda.sserafimflow.exceptions.GeneralExceptions.ConnectionFailed;
import xyz.brianuceda.sserafimflow.implementations.ExchangeRateImpl;
import xyz.brianuceda.sserafimflow.respositories.ExchangeRateRepository;

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

    List<ExchangeRateEntity> exchangeRateEntities = exchangeRateRepository.findByDate(currentDate);
    ExchangeRateDTO exchangeRateDTO = null;
    ExchangeRateEntity exchangeRateEntity = null;

    // Si no existe en la BD o hay múltiples entradas
    if (exchangeRateEntities.isEmpty()) {
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
      // Si existe en la BD, usar la primera entrada encontrada
      exchangeRateEntity = exchangeRateEntities.get(0);
      
      // Si hay múltiples entradas con la misma fecha, eliminar las duplicadas excepto la primera
      if (exchangeRateEntities.size() > 1) {
        for (int i = 1; i < exchangeRateEntities.size(); i++) {
          exchangeRateRepository.delete(exchangeRateEntities.get(i));
        }
      }
      
      // Convertir a DTO para retornar
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
