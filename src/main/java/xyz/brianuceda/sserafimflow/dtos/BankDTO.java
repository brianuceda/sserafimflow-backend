package xyz.brianuceda.sserafimflow.dtos;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

import xyz.brianuceda.sserafimflow.entities.BankEntity;
import xyz.brianuceda.sserafimflow.enums.CurrencyEnum;
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
  private String publicUuid;
  private String imageUrl;
  private CurrencyEnum mainCurrency;
  private CurrencyEnum previewDataCurrency;
  private BigDecimal balance;
  private BigDecimal nominalRate;
  private BigDecimal effectiveRate;
  private BigDecimal extraCommission;
  private LocalDate creationDate;
  private Timestamp accountCreationDate;
  
  public BankDTO(BankEntity bank, boolean includeId) {
    if (includeId) {
      this.id = bank.getId();
    } 
    this.realName = bank.getRealName();
    this.ruc = bank.getRuc();
    this.username = bank.getUsername();
    this.publicUuid = bank.getPublicUuid();
    this.imageUrl = bank.getImageUrl();
    this.mainCurrency = bank.getMainCurrency();
    this.previewDataCurrency = bank.getPreviewDataCurrency();
    this.balance = bank.getBalance();
    this.nominalRate = bank.getNominalRate();
    this.effectiveRate = bank.getEffectiveRate();
    this.extraCommission = bank.getExtraCommission();
    this.creationDate = bank.getCreationDate();
    this.accountCreationDate = bank.getAccountCreationDate();
  }

  // Para mostrar en los documentos de la empresa comprados por el banco
  public BankDTO(Long id, String realName, String imageUrl) {
    this.id = id;
    this.realName = realName;
    this.imageUrl = imageUrl;
  }

  // Para mostrar en la compra
  public BankDTO(String realName, String ruc, String imageUrl, BigDecimal nominalRate, BigDecimal effectiveRate, BigDecimal extraCommission) {
    this.realName = realName;
    this.ruc = ruc;
    this.imageUrl = imageUrl;
    this.nominalRate = nominalRate;
    this.effectiveRate = effectiveRate;
    this.extraCommission = extraCommission;
  }
}
