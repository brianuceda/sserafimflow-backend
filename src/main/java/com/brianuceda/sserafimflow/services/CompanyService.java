package com.brianuceda.sserafimflow.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.brianuceda.sserafimflow.dtos.CompanyDTO;
import com.brianuceda.sserafimflow.dtos.CompanyDashboard;
import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;
import com.brianuceda.sserafimflow.entities.CompanyEntity;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.brianuceda.sserafimflow.enums.RateTypeEnum;
import com.brianuceda.sserafimflow.implementations.CompanyImpl;
import com.brianuceda.sserafimflow.respositories.CompanyRepository;
import com.brianuceda.sserafimflow.utils.PurchaseUtils;

import jakarta.persistence.Tuple;

@Service
public class CompanyService implements CompanyImpl {
  private final CompanyRepository companyRepository;
  private final PurchaseUtils purchaseUtils;

  public CompanyService(CompanyRepository companyRepository, PurchaseUtils purchaseUtils) {
    this.companyRepository = companyRepository;
    this.purchaseUtils = purchaseUtils;
  }

  @Override
  public CompanyDashboard getDashboard(String username, CurrencyEnum targetCurrency) {
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

    CompanyDashboard dashboard = new CompanyDashboard();

    if (targetCurrency == null) {
      targetCurrency = company.getPreviewDataCurrency();
    }

    // Tasa de cambio del día
    dashboard.setTodayExchangeRate(this.purchaseUtils.getTodayExchangeRate());
    dashboard.setMainCurrency(targetCurrency);

    // Datos generales
    this.accumulateGeneralData(dashboard, company.getId(), dashboard.getMainCurrency(), dashboard.getTodayExchangeRate());

    // Datos mensuales
    this.accumulateMonthlyData(dashboard, company.getId(), dashboard.getMainCurrency(), dashboard.getTodayExchangeRate());

    return dashboard;
  }

  private void accumulateGeneralData(CompanyDashboard dashboard, Long companyId, CurrencyEnum targetCurrency,
      ExchangeRateDTO exchangeRateDTO) {
    List<Tuple> purchases = companyRepository.getDetailedPurchases(companyId);

    if (purchases.isEmpty()) {
      return;
    }

    // Variables para acumular valores
    BigDecimal totalNominalValueIssued = BigDecimal.ZERO;
    BigDecimal totalNominalValueReceived = BigDecimal.ZERO;
    BigDecimal totalNominalValueDiscounted = BigDecimal.ZERO;
    Map<String, Integer> currencyCount = new HashMap<>();
    Map<String, Integer> rateTypeCount = new HashMap<>();
    Map<String, Integer> bankCount = new HashMap<>();

    for (Tuple purchase : purchases) {
      BigDecimal nominalValue = (BigDecimal) purchase.get("nominalValue");
      BigDecimal receivedValue = (BigDecimal) purchase.get("receivedValue");
      BigDecimal discountedValue = (BigDecimal) purchase.get("discountedValue");
      CurrencyEnum fromCurrency = CurrencyEnum.valueOf(purchase.get("currency", String.class));
      RateTypeEnum rateType = RateTypeEnum.valueOf(purchase.get("rateType", String.class));
      String bankRealName = purchase.get("bankRealName", String.class);

      // Convertir cada valor a la moneda objetivo
      nominalValue = purchaseUtils.convertCurrency(nominalValue, fromCurrency, targetCurrency, exchangeRateDTO);
      receivedValue = purchaseUtils.convertCurrency(receivedValue, fromCurrency, targetCurrency, exchangeRateDTO);
      discountedValue = purchaseUtils.convertCurrency(discountedValue, fromCurrency, targetCurrency, exchangeRateDTO);

      // Sumar los valores convertidos
      totalNominalValueIssued = totalNominalValueIssued.add(nominalValue);
      totalNominalValueReceived = totalNominalValueReceived.add(receivedValue);
      totalNominalValueDiscounted = totalNominalValueDiscounted.add(discountedValue);

      // Contar las divisas y tipos de tasa
      currencyCount.put(fromCurrency.name(), currencyCount.getOrDefault(fromCurrency.name(), 0) + 1);
      rateTypeCount.put(rateType.name(), rateTypeCount.getOrDefault(rateType.name(), 0) + 1);

      // Contar la frecuencia del banco
      bankCount.put(bankRealName, bankCount.getOrDefault(bankRealName, 0) + 1);
    }

    dashboard.setTotalNominalValueIssued(totalNominalValueIssued);
    dashboard.setTotalNominalValueReceived(totalNominalValueReceived);
    dashboard.setTotalNominalValueDiscounted(totalNominalValueDiscounted);

    // Encontrar las tasas más usadas
    String mostUsedRateType = rateTypeCount.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);

    String mostUsedCurrency = currencyCount.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);
      
    // Encontrar el banco más usado
    String mostUsedBankRealName = bankCount.entrySet().stream()
      .max(Map.Entry.comparingByValue())
      .map(Map.Entry::getKey)
      .orElse(null);

    dashboard.setMostUsedPeriodRate(RateTypeEnum.valueOf(mostUsedRateType));
    dashboard.setMostUsedCurrency(CurrencyEnum.valueOf(mostUsedCurrency));
    dashboard.setMostUsedBankForSales(mostUsedBankRealName);
  }

  private void accumulateMonthlyData(CompanyDashboard dashboard, Long companyId, CurrencyEnum targetCurrency,
      ExchangeRateDTO exchangeRateDTO) {
    List<Tuple> purchases = companyRepository.getDetailedPurchases(companyId);

    // Listas con valores de 0 para cada mes
    List<Integer> cantSoldLettersPerMonth = new ArrayList<>(Collections.nCopies(12, 0));
    List<Integer> cantSoldInvoicesPerMonth = new ArrayList<>(Collections.nCopies(12, 0));
    List<BigDecimal> amountSoldLettersPerMonth = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));
    List<BigDecimal> amountSoldInvoicesPerMonth = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));

    for (Tuple purchase : purchases) {
      int monthIndex = ((Number) purchase.get("month")).intValue() - 1;
      BigDecimal receivedValue = (BigDecimal) purchase.get("receivedValue");
      CurrencyEnum fromCurrency = CurrencyEnum.valueOf(purchase.get("currency", String.class));
      String documentType = purchase.get("documentType", String.class);

      // Convertir el valor recibido a la moneda objetivo
      receivedValue = purchaseUtils.convertCurrency(receivedValue, fromCurrency, targetCurrency, exchangeRateDTO);

      // Acumular valores según el tipo de documento
      if ("LETTER".equals(documentType)) {
        cantSoldLettersPerMonth.set(monthIndex, cantSoldLettersPerMonth.get(monthIndex) + 1);
        amountSoldLettersPerMonth.set(monthIndex, amountSoldLettersPerMonth.get(monthIndex).add(receivedValue));
      } else if ("INVOICE".equals(documentType)) {
        cantSoldInvoicesPerMonth.set(monthIndex, cantSoldInvoicesPerMonth.get(monthIndex) + 1);
        amountSoldInvoicesPerMonth.set(monthIndex, amountSoldInvoicesPerMonth.get(monthIndex).add(receivedValue));
      }
    }

    // Establecer los valores calculados
    dashboard.setCantSoldLettersPerMonth(cantSoldLettersPerMonth);
    dashboard.setCantSoldInvoicesPerMonth(cantSoldInvoicesPerMonth);
    dashboard.setAmountSoldLettersPerMonth(amountSoldLettersPerMonth);
    dashboard.setAmountSoldInvoicesPerMonth(amountSoldInvoicesPerMonth);
  }

  @Override
  public CompanyDTO getProfile(String username) {
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

    return new CompanyDTO(company, false);
  }

  @Override
  public CompanyDTO updateCompanyProfile(String username, CompanyDTO companyDTO) {
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

    List<String> fieldsUpdated = new ArrayList<>();

    if (companyDTO.getRealName() != null && !companyDTO.getRealName().isEmpty() &&
        !companyDTO.getRealName().equals(company.getRealName())) {
      company.setRealName(companyDTO.getRealName());
      fieldsUpdated.add("realName");
    }

    if (companyDTO.getRuc() != null && !companyDTO.getRuc().isEmpty() &&
        !companyDTO.getRuc().equals(company.getRuc())) {
      company.setRuc(companyDTO.getRuc());
      fieldsUpdated.add("ruc");
    }

    if (companyDTO.getUsername() != null && !companyDTO.getUsername().isEmpty() &&
        !companyDTO.getUsername().equals(company.getUsername())) {
      company.setUsername(companyDTO.getUsername());
      fieldsUpdated.add("username");
    }

    // Realizar la conversión de moneda si es necesario
    if (companyDTO.getMainCurrency() != null &&
        !companyDTO.getMainCurrency().equals(company.getMainCurrency())) {

      CurrencyEnum oldCurrency = company.getMainCurrency();
      CurrencyEnum newCurrency = companyDTO.getMainCurrency();
      BigDecimal currentBalance = company.getBalance();

      ExchangeRateDTO exchangeRateDTO = purchaseUtils.getTodayExchangeRate();

      BigDecimal convertedBalance = purchaseUtils.convertCurrency(currentBalance, oldCurrency, newCurrency,
          exchangeRateDTO);

      company.setBalance(convertedBalance);
      company.setMainCurrency(newCurrency);

      fieldsUpdated.add("mainCurrency");
    }

    if (companyDTO.getPreviewDataCurrency() != null &&
        !companyDTO.getPreviewDataCurrency().equals(company.getPreviewDataCurrency())) {
      company.setPreviewDataCurrency(companyDTO.getPreviewDataCurrency());
      fieldsUpdated.add("previewDataCurrency");
    }

    companyRepository.save(company);

    return new CompanyDTO(company, false);
  }

}
