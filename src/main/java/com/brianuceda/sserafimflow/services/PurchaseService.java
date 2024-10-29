package com.brianuceda.sserafimflow.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;
import com.brianuceda.sserafimflow.dtos.PurchasedDocumentDTO;
import com.brianuceda.sserafimflow.dtos.RegisterPurchaseDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.entities.BankEntity;
import com.brianuceda.sserafimflow.entities.CompanyEntity;
import com.brianuceda.sserafimflow.entities.DocumentEntity;
import com.brianuceda.sserafimflow.entities.PurchaseEntity;
import com.brianuceda.sserafimflow.enums.AuthRoleEnum;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.brianuceda.sserafimflow.enums.StateEnum;
import com.brianuceda.sserafimflow.implementations.PurchaseImpl;
import com.brianuceda.sserafimflow.respositories.BankRepository;
import com.brianuceda.sserafimflow.respositories.CompanyRepository;
import com.brianuceda.sserafimflow.respositories.DocumentRepository;
import com.brianuceda.sserafimflow.respositories.PurchaseRepository;

import jakarta.transaction.Transactional;
import lombok.extern.java.Log;

@Service
@Log
public class PurchaseService implements PurchaseImpl {
  private final BankRepository bankRepository;
  private final CompanyRepository companyRepository;
  private final DocumentRepository documentRepository;
  private final PurchaseRepository purchaseRepository;
  private final ExchangeRateService exchangeRateService;

  public PurchaseService(BankRepository bankRepository,
      CompanyRepository companyRepository,
      DocumentRepository documentRepository,
      PurchaseRepository purchaseRepository,
      ExchangeRateService exchangeRateService) {

    this.bankRepository = bankRepository;
    this.companyRepository = companyRepository;
    this.documentRepository = documentRepository;
    this.purchaseRepository = purchaseRepository;
    this.exchangeRateService = exchangeRateService;
  }

  // Company
  @Override
  @Transactional
  public ResponseDTO sellDocument(String username, RegisterPurchaseDTO purchaseDTO) {
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

    DocumentEntity document = documentRepository.findByIdAndCompanyId(purchaseDTO.getDocumentId(), company.getId())
        .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));

    if (document.getState() == StateEnum.PENDING || document.getState() == StateEnum.PAID) {
      throw new IllegalArgumentException("El documento ya fue vendido");
    }

    BankEntity bank = bankRepository.findById(purchaseDTO.getBankId())
        .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado"));

    // Conversión de moneda si es necesario
    BigDecimal nominalValue = document.getAmount();
    if (document.getCurrency() != bank.getCurrency()) {
      nominalValue = convertCurrency(nominalValue, document.getCurrency(), bank.getCurrency());
    }

    // Si no se especifica la fecha de compra, se toma la fecha actual
    LocalDateTime purchaseDate = purchaseDTO.getPurchaseDate() != null
        ? purchaseDTO.getPurchaseDate().atStartOfDay()
        : LocalDateTime.now();

    Integer days = (int) ChronoUnit.DAYS.between(purchaseDate.toLocalDate(), document.getDueDate());

    // TEP y Tasa Descontada
    BigDecimal calculatedOrUsedTEP = null;
    BigDecimal rateUsed = null;

    switch (purchaseDTO.getRateType()) {
      case NOMINAL:
        calculatedOrUsedTEP = calculateTep(bank.getNominalRate(), days);
        rateUsed = bank.getNominalRate();
        break;

      case EFFECTIVE:
        calculatedOrUsedTEP = bank.getEffectiveRate();
        rateUsed = bank.getEffectiveRate();
        break;

      default:
        throw new IllegalArgumentException("Tipo de tasa no permitido");
    }

    BigDecimal discountRate = calculateDiscountRate(calculatedOrUsedTEP);
    BigDecimal receivedValue = calculateReceivedValue(document.getAmount(), discountRate);

    // Marca en proceso de venta el documento
    document.setState(StateEnum.PENDING);

    // Crear la compra
    PurchaseEntity purchase = PurchaseEntity.builder()
        .purchaseDate(purchaseDate.toLocalDate())
        .currency(bank.getCurrency())
        .nominalValue(nominalValue)
        .discountRate(discountRate)
        .receivedValue(receivedValue)
        .days((int) days)
        .tep(calculatedOrUsedTEP)
        .rateType(purchaseDTO.getRateType())
        .rateValue(rateUsed)
        .state(StateEnum.PENDING)
        .bank(bank)
        .document(document)
        .build();

    // Guardar la compra y asociar el documento
    documentRepository.save(document);
    purchaseRepository.save(purchase);

    return new ResponseDTO("Venta registrada con éxito");
  }

  private BigDecimal convertCurrency(BigDecimal amount, CurrencyEnum fromCurrency, CurrencyEnum toCurrency) {
    ExchangeRateDTO exchangeRateDTO = this.exchangeRateService.getTodayExchangeRate();

    if (fromCurrency == CurrencyEnum.USD && toCurrency == CurrencyEnum.PEN) {
      // Convertir de USD a PEN usando la tasa de venta
      return amount.multiply(exchangeRateDTO.getCurrencyRates().get(0).getSalePrice());
    } else if (fromCurrency == CurrencyEnum.PEN && toCurrency == CurrencyEnum.USD) {
      // Convertir de PEN a USD usando la tasa de compra
      return amount.divide(exchangeRateDTO.getCurrencyRates().get(0).getPurchasePrice(), RoundingMode.HALF_UP);
    }

    return amount;
  }

  private BigDecimal calculateTep(BigDecimal nominalRate, Integer days) {
    double m = 360;
    return BigDecimal.valueOf(Math.pow(1 + nominalRate.doubleValue() / m, days / 360.0 * m) - 1);
  }

  private BigDecimal calculateDiscountRate(BigDecimal tep) {
    return tep.divide(BigDecimal.ONE.add(tep), RoundingMode.HALF_UP);
  }

  private BigDecimal calculateReceivedValue(BigDecimal nominalValue, BigDecimal discountRate) {
    return nominalValue.multiply(BigDecimal.ONE.subtract(discountRate));
  }

  @Override
  public List<PurchasedDocumentDTO> getPurchasesBySpecificState(String username, AuthRoleEnum role,
      StateEnum state) {
    switch (role) {
      case COMPANY: {
        CompanyEntity company = companyRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        // Obtener las compras de la empresa según el estado
        List<PurchaseEntity> purchases = null;
        if (state != null) {
          purchases = purchaseRepository.findAllByStateAndDocumentCompanyId(state, company.getId());
        } else {
          purchases = purchaseRepository.findAllByDocumentCompanyId(company.getId());
        }

        if (purchases.isEmpty()) {
          return List.of();
        }

        // Ordenar por ID
        purchases.sort((b1, b2) -> b1.getId().compareTo(b2.getId()));

        // Convertir a DTO y retornar
        return this.convertEntityListToDTOList(purchases, role, true);
      }

      case BANK: {
        BankEntity bank = bankRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado"));

        // Obtener las compras del banco según el estado
        List<PurchaseEntity> purchases = null;
        if (state != null) {
          purchases = purchaseRepository.findAllByStateAndBankId(state, bank.getId());
        } else {
          purchases = purchaseRepository.findAllByBankId(bank.getId());
        }

        if (purchases.isEmpty()) {
          return List.of();
        }

        // Ordenar por ID
        purchases.sort((b1, b2) -> b1.getId().compareTo(b2.getId()));

        // Convertir a DTO y retornar
        return this.convertEntityListToDTOList(purchases, role, true);
      }
      default: {
        throw new IllegalArgumentException("Rol no permitido");
      }
    }
  }

  private List<PurchasedDocumentDTO> convertEntityListToDTOList(List<PurchaseEntity> purchases, AuthRoleEnum role,
      boolean includeId) {
    List<PurchasedDocumentDTO> purchasesDTO = new ArrayList<>();
    for (PurchaseEntity purchase : purchases) {
      PurchasedDocumentDTO purchaseDTO = new PurchasedDocumentDTO(purchase);

      if (!includeId) {
        purchaseDTO.setId(null);
      }

      purchasesDTO.add(purchaseDTO);
    }
    return purchasesDTO;
  }

  // Bank
  @Override
  @Transactional
  public ResponseDTO payDocument(String username, Long purchaseId) {

    BankEntity bank = bankRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado"));

    PurchaseEntity purchase = purchaseRepository.findByIdAndBankId(purchaseId, bank.getId())
        .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada"));

    if (purchase.getState() == StateEnum.PAID) {
      throw new IllegalArgumentException("El pago ya fue realizado");
    }

    log.info("- Realizando pago de compra con ID: " + purchaseId + " - Por el banco: " + bank.getRealName());
    log.info("- Monto a pagar: " + purchase.getReceivedValue());
    log.info("- Fecha de pago: " + LocalDateTime.now());

    // Cambiar cantidades de dinero
    BigDecimal amount = purchase.getReceivedValue();

    // if (bank.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
    // throw new IllegalArgumentException("Fondos insuficientes");
    // }

    CompanyEntity company = purchase.getDocument().getCompany();

    bank.setBalance(bank.getBalance().subtract(amount));
    company.setBalance(company.getBalance().add(amount));

    // Cambiar estados
    purchase.getDocument().setState(StateEnum.PAID);
    purchase.setState(StateEnum.PAID);
    purchase.setPayDate(LocalDateTime.now());

    // Guardar cambios
    bankRepository.save(bank);
    companyRepository.save(company);
    documentRepository.save(purchase.getDocument());
    purchaseRepository.save(purchase);

    return new ResponseDTO("Pago realizado con éxito");
  }

  // Como banco, decido cuando pagar las compras
  // Si la fecha de expiracion del documento (dueDate) está a menos de 12 horas o
  // ya pasó, la compra se realiza automáticamente
  // Si el banco no tiene dinero suficiente, la compra se realiza de todos modos y
  // el banco queda en negativo
  @Override
  public void tryToBuyByPurchaseDate() {
    List<PurchaseEntity> purchases = purchaseRepository.findAllByState(StateEnum.PENDING);

    log.info("Cantidad de compras pendientes de pago: " + purchases.size());

    for (PurchaseEntity purchase : purchases) {
      if (purchase.getPurchaseDate().atStartOfDay().isBefore(LocalDateTime.now())) {
        this.payDocument(purchase.getBank().getUsername(), purchase.getId());
      }
    }
  }
}
