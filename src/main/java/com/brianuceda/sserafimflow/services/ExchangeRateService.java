package com.brianuceda.sserafimflow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;
import com.brianuceda.sserafimflow.entities.ExchangeRateEntity;
import com.brianuceda.sserafimflow.exceptions.GeneralExceptions.ConnectionFailed;
import com.brianuceda.sserafimflow.implementations.ExchangeRateServiceImpl;
import com.brianuceda.sserafimflow.respositories.ExchangeRateRepository;

import java.time.LocalDate;

@Service
public class ExchangeRateService implements ExchangeRateServiceImpl {
  @Value("${SUNAT_TOKEN}")
  private String sunatToken;

  @Autowired
  private ExchangeRateRepository exchangeRateRepository;

  @Override
  public ExchangeRateDTO getExchangeRateApi(LocalDate fecha) throws ConnectionFailed {
    ExchangeRateEntity exchangeRateEntity = exchangeRateRepository.findByFecha(fecha);
    ExchangeRateDTO exchangeRateDTO = null;

    // Si no existe en la BD, La obtiene de SUNAT y la guarda en la BD
    if (exchangeRateEntity == null) {
      exchangeRateDTO = this.getExchangeRateFromSunatApi(fecha);
      exchangeRateRepository.save(new ExchangeRateEntity(exchangeRateDTO));
    } else {
      exchangeRateDTO = new ExchangeRateDTO(exchangeRateEntity);
    }

    return exchangeRateDTO;
  }

  private ExchangeRateDTO getExchangeRateFromSunatApi(LocalDate fecha) {
    // Prueba
    // ExchangeRateDTO exchangeRateDTO = new ExchangeRateDTO(new BigDecimal("3.744"), new BigDecimal("3.751"), "USD", fecha);
    // return exchangeRateDTO;

    try {
      String url = "https://api.apis.net.pe/v2/sunat/tipo-cambio?date=" + fecha; // 2024-09-20
  
      RestTemplate restTemplate = new RestTemplate();
  
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "Bearer " + this.sunatToken);
      headers.set("Referer", "https://apis.net.pe/tipo-de-cambio-sunat-api");
  
      // Entidad de solicitud (con encabezados)
      HttpEntity<String> entity = new HttpEntity<>(headers);
  
      // Realizar solicitud
      ResponseEntity<ExchangeRateDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, ExchangeRateDTO.class);

      // Obtener respuesta
      ExchangeRateDTO tasaDeCambio = response.getBody();

      return tasaDeCambio;
    } catch (Exception e) {
      throw new ConnectionFailed("No se pudo obtener la tasa de cambio de la fecha " + fecha);
    }
  }
}
