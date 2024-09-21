package com.brianuceda.sserafimflow.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exchange_rate")
public class ExchangeRateEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(precision = 4, scale = 3)
  @Positive
  private BigDecimal precioCompra;
  
  @Column(precision = 4, scale = 3)
  @Positive
  private BigDecimal precioVenta;

  @Column(length = 3)
  private String moneda; // USD

  private LocalDate fecha; // yyyy-MM-dd

  public ExchangeRateEntity(ExchangeRateDTO exchangeRateDTO) {
    this.precioCompra = exchangeRateDTO.getPrecioCompra();
    this.precioVenta = exchangeRateDTO.getPrecioVenta();
    this.moneda = exchangeRateDTO.getMoneda();
    this.fecha = exchangeRateDTO.getFecha();
  }
}
