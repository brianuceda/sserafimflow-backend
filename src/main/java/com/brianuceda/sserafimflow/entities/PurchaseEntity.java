package com.brianuceda.sserafimflow.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.brianuceda.sserafimflow.dtos.purchase.PurchasedDocumentDTO;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.brianuceda.sserafimflow.enums.StateEnum;
import com.brianuceda.sserafimflow.enums.RateTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "purchase")
public class PurchaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private LocalDate purchaseDate; // Fecha de compra

  @Column(nullable = true)
  private LocalDate payDate; // Fecha de cobro

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CurrencyEnum currency; // Moneda

  @Column(precision = 16, scale = 4, nullable = false)
  @Positive
  private BigDecimal nominalValue; // Valor nominal
  
  @Column(precision = 5, scale = 4, nullable = false)
  @Positive
  private BigDecimal discountRate; // Tasa descontada

  @Column(precision = 16, scale = 4, nullable = false)
  @Positive
  private BigDecimal receivedValue; // Valor recibido

  @Column(nullable = false)
  private Integer days; // Dias
  
  @Column(precision = 5, scale = 4, nullable = false)
  @Positive
  private BigDecimal tep; // Tasa efectiva del periodo

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private RateTypeEnum rateType; // Tipo de tasa

  @Column(precision = 5, scale = 4, nullable = false)
  @Positive
  private BigDecimal rateValue; // Valor de la tasa

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private StateEnum state; // Estado de la compra

  @ManyToOne
  @JoinColumn(name = "document_id")
  private DocumentEntity document;

  @ManyToOne
  @JoinColumn(name = "bank_id")
  private BankEntity bank;

  public PurchaseEntity(PurchasedDocumentDTO purchaseDTO) {
    this.id = purchaseDTO.getId();
    this.purchaseDate = purchaseDTO.getPurchaseDate();
    this.currency = purchaseDTO.getCurrency();
    this.nominalValue = purchaseDTO.getNominalValue();
    this.discountRate = purchaseDTO.getDiscountRate();
    this.receivedValue = purchaseDTO.getReceivedValue();
    this.days = purchaseDTO.getDays();
    this.tep = purchaseDTO.getTep();
    this.rateType = purchaseDTO.getRateType();
    this.rateValue = purchaseDTO.getRateValue();
    this.state = purchaseDTO.getState();
  }
}
