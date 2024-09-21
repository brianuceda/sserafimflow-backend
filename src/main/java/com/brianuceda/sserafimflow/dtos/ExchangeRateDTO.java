package com.brianuceda.sserafimflow.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.brianuceda.sserafimflow.entities.ExchangeRateEntity;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateDTO {
  private BigDecimal precioCompra;
  private BigDecimal precioVenta;
  private String moneda;
  private LocalDate fecha;

  public ExchangeRateDTO(ExchangeRateEntity exchangeRateEntity) {
    this.precioCompra = exchangeRateEntity.getPrecioCompra();
    this.precioVenta = exchangeRateEntity.getPrecioVenta();
    this.moneda = exchangeRateEntity.getMoneda();
    this.fecha = exchangeRateEntity.getFecha();
  }
}
