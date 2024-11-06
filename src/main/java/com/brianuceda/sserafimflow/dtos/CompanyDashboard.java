package com.brianuceda.sserafimflow.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.brianuceda.sserafimflow.enums.RateTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyDashboard {
  private BigDecimal totalNominalValueIssued;
  private BigDecimal totalNominalValueReceived;
  private BigDecimal totalNominalValueDiscounted;
  private Integer pendingPortfoliosToPay;
  private RateTypeEnum mostUsedPeriodRate;
  private CurrencyEnum mostUsedCurrency;
  private List<Integer> cantSoldLettersPerMonth;
  private List<Integer> cantSoldInvoicesPerMonth;
  private List<BigDecimal> amountSoldLettersPerMonth;
  private List<BigDecimal> amountSoldInvoicesPerMonth;
  private ExchangeRateDTO exchangeRate;
  
  public CompanyDashboard() {
    this.totalNominalValueIssued = new BigDecimal(0);
    this.totalNominalValueReceived = new BigDecimal(0);
    this.totalNominalValueDiscounted = new BigDecimal(0);
    this.pendingPortfoliosToPay = 0;
    this.cantSoldLettersPerMonth = new ArrayList<>();
    this.cantSoldInvoicesPerMonth = new ArrayList<>();
    this.amountSoldLettersPerMonth = new ArrayList<>();
    this.amountSoldInvoicesPerMonth = new ArrayList<>();
    this.exchangeRate = new ExchangeRateDTO();
  }
}
