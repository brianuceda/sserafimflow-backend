package xyz.brianuceda.sserafimflow.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import xyz.brianuceda.sserafimflow.dtos.CurrencyRateDTO;
import xyz.brianuceda.sserafimflow.enums.CurrencyEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "currency_rate")
public class CurrencyRateEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CurrencyEnum currency;

  @Column(precision = 5, scale = 4, nullable = true)
  @Positive
  private BigDecimal purchasePrice;

  @Column(precision = 5, scale = 4, nullable = true)
  @Positive
  private BigDecimal salePrice;

  @ManyToOne
  @JoinColumn(name = "exchange_rate_id")
  private ExchangeRateEntity exchangeRate;

  public CurrencyRateEntity(CurrencyRateDTO currencyRateDTO) {
    this.currency = currencyRateDTO.getCurrency();
    this.purchasePrice = currencyRateDTO.getPurchasePrice();
    this.salePrice = currencyRateDTO.getSalePrice();
  }
  
  @Override
  public String toString() {
    return "CurrencyRateEntity(id=" + id + 
           ", currency=" + currency + 
           ", purchasePrice=" + purchasePrice + 
           ", salePrice=" + salePrice + ")";
  }
}
