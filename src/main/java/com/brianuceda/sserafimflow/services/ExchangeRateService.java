package com.brianuceda.sserafimflow.services;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.brianuceda.sserafimflow.dtos.CurrencyRateDTO;
import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;
import com.brianuceda.sserafimflow.entities.CurrencyRateEntity;
import com.brianuceda.sserafimflow.entities.ExchangeRateEntity;
import com.brianuceda.sserafimflow.exceptions.GeneralExceptions.ConnectionFailed;
import com.brianuceda.sserafimflow.implementations.ExchangeRateServiceImpl;
import com.brianuceda.sserafimflow.respositories.ExchangeRateRepository;
import com.brianuceda.sserafimflow.utils.SeleniumUtils;

import java.time.LocalDate;
import java.math.BigDecimal;

import java.time.LocalDateTime;

@Service
public class ExchangeRateService implements ExchangeRateServiceImpl {
  @Value("${IS_PRODUCTION}")
  private boolean isProduction;

  @Value("${SUNAT_TOKEN}")
  private String sunatToken;

  private final ExchangeRateRepository exchangeRateRepository;
  private final SeleniumUtils seleniumUtils;

  public ExchangeRateService(ExchangeRateRepository exchangeRateRepository, SeleniumUtils seleniumUtils) {
    this.exchangeRateRepository = exchangeRateRepository;
    this.seleniumUtils = seleniumUtils;
  }

  @Override
  public ExchangeRateDTO getTodayExchangeRate() throws ConnectionFailed {
    // Fecha actual (Prod: UTC / Dev: GMT-5)
    LocalDate currentDate = this.isProduction ? LocalDateTime.now().minusHours(5).toLocalDate() : LocalDate.now();

    // Buscar en la BD
    ExchangeRateEntity exchangeRateEntity = exchangeRateRepository.findByDate(currentDate);
    ExchangeRateDTO exchangeRateDTO = null;

    // Si no existe en la BD
    if (exchangeRateEntity == null) {
      // Obtener de la página de la SBS
      exchangeRateDTO = this.getExchangeRateFromSbsPage(currentDate);

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
    }

    return exchangeRateDTO;
  }

  private ExchangeRateDTO getExchangeRateFromSbsPage(LocalDate currentDate) throws ConnectionFailed {
    ThreadLocal<RemoteWebDriver> driver = new ThreadLocal<>();

    try {
      seleniumUtils.setUp(driver);
      driver.get().get("https://www.sbs.gob.pe/app/pp/sistip_portal/paginas/publicacion/tipocambiopromedio.aspx");

      // Esperar a que cargue el contenido
      SeleniumUtils.waitUntilTextChanges(driver.get(), By.className("APLI_contenidoInterno"));

      // Extraer las filas de la tabla usando un XPath apropiado
      List<WebElement> rows = driver.get()
          .findElements(By.xpath("//table[@id='ctl00_cphContent_rgTipoCambio_ctl00']/tbody/tr"));

      ExchangeRateDTO exchangeRateDTO = new ExchangeRateDTO();
      exchangeRateDTO.setDate(currentDate);

      for (WebElement row : rows) {
        List<WebElement> cells = row.findElements(By.tagName("td"));

        String currency = cells.get(0).getText().trim();

        if (currency.equals("Dólar de N.A.")) {
          currency = "USD";
        } else if (currency.equals("Dólar Canadiense")) {
          currency = "CAD";
        } else if (currency.equals("Euro")) {
          currency = "EUR";
        } else {
          continue;
        }

        String purchasePriceStr = cells.get(1).getText().trim().isEmpty() ? null : cells.get(1).getText().trim();
        String salePriceStr = cells.get(2).getText().trim().isEmpty() ? null : cells.get(2).getText().trim();

        BigDecimal purchasePriceBD = purchasePriceStr == null ? null : new BigDecimal(purchasePriceStr);
        BigDecimal salePriceBD = salePriceStr == null ? null : new BigDecimal(salePriceStr);

        if (purchasePriceBD == null || salePriceBD == null) {
          continue;
        }

        CurrencyRateDTO currencyRateDTO = new CurrencyRateDTO(currency, purchasePriceBD, salePriceBD);

        exchangeRateDTO.getCurrencyRates().add(currencyRateDTO);
      }

      return exchangeRateDTO;
    } catch (Exception ex) {
      // throw new ConnectionFailed("No se pudo obtener la tasa de cambio de la SBS");
      throw new ConnectionFailed(ex.getMessage());
    } finally {
      seleniumUtils.closeBrowser(driver);
    }
  }

}
