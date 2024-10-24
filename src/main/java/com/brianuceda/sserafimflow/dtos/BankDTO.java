package com.brianuceda.sserafimflow.dtos;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.brianuceda.sserafimflow.entities.BankEntity;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankDTO {
  private Long id;
  private String realName;
  private String ruc;
  private String username;
  private String password;
  private String imageUrl;
  private CurrencyEnum currency;
  private BigDecimal balance;
  private BigDecimal nominalRate;
  private BigDecimal effectiveRate;
  private BigDecimal extraCommission;
  private Timestamp creationDate;
  
  public BankDTO(BankEntity bank, boolean includeId) {
    if (includeId) {
      this.id = bank.getId();
    } 
    this.realName = bank.getRealName();
    this.ruc = bank.getRuc();
    this.username = bank.getUsername();
    this.imageUrl = bank.getImageUrl();
    this.currency = bank.getCurrency();
    this.balance = bank.getBalance();
    this.nominalRate = bank.getNominalRate();
    this.effectiveRate = bank.getEffectiveRate();
    this.extraCommission = bank.getExtraCommission();
    this.creationDate = bank.getCreationDate();
  }

  // Para mostrar en los documentos de la empresa comprados por el banco
  public BankDTO(Long id, String realName, String ruc, CurrencyEnum currency) {
    this.id = id;
    this.realName = realName;
    this.ruc = ruc;
    this.currency = currency;
  }
}