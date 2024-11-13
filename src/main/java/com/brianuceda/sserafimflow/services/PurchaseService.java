package com.brianuceda.sserafimflow.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.brianuceda.sserafimflow.dtos.CompanyDashboard;
import com.brianuceda.sserafimflow.dtos.ExchangeRateDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.dtos.purchase.PurchaseEquationsDTO;
import com.brianuceda.sserafimflow.dtos.purchase.PurchaseEquationsDTO.*;
import com.brianuceda.sserafimflow.dtos.purchase.PurchasedDocumentDTO;
import com.brianuceda.sserafimflow.dtos.purchase.RegisterPurchaseDTO;
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
import com.brianuceda.sserafimflow.utils.PurchaseUtils;

import jakarta.transaction.Transactional;
import lombok.extern.java.Log;

@Service
@Log
public class PurchaseService implements PurchaseImpl {
  private final BankRepository bankRepository;
  private final CompanyRepository companyRepository;
  private final DocumentRepository documentRepository;
  private final PurchaseRepository purchaseRepository;
  private final CompanyService companyService;
  private final CompanyUpdateService companyUpdateService;
  private final PurchaseUtils purchaseUtils;

  public PurchaseService(BankRepository bankRepository,
      CompanyRepository companyRepository,
      DocumentRepository documentRepository,
      PurchaseRepository purchaseRepository,
      CompanyService companyService,
      CompanyUpdateService companyUpdateService,
      PurchaseUtils purchaseUtils) {

    this.bankRepository = bankRepository;
    this.companyRepository = companyRepository;
    this.documentRepository = documentRepository;
    this.purchaseRepository = purchaseRepository;
    this.companyService = companyService;
    this.companyUpdateService = companyUpdateService;
    this.purchaseUtils = purchaseUtils;
  }

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
    if (document.getCurrency() != bank.getMainCurrency()) {
      ExchangeRateDTO exchangeRateDTO = this.purchaseUtils.getTodayExchangeRate();
      nominalValue = this.purchaseUtils.convertCurrency(nominalValue, document.getCurrency(), bank.getMainCurrency(),
          exchangeRateDTO);
    }
    nominalValue = nominalValue.setScale(4, RoundingMode.HALF_UP); // Limitar a 4 decimales

    Integer days = (int) ChronoUnit.DAYS.between(document.getDiscountDate(), document.getExpirationDate());

    // TEP y Tasa Descontada
    BigDecimal calculatedOrUsedTEP = null;
    BigDecimal rateUsed = null;

    switch (purchaseDTO.getRateType()) {
      case NOMINAL:
        calculatedOrUsedTEP = calculateTep(bank.getNominalRate(), days).setScale(4, RoundingMode.HALF_UP);
        rateUsed = bank.getNominalRate().setScale(4, RoundingMode.HALF_UP);
        break;

      case EFFECTIVE:
        calculatedOrUsedTEP = bank.getEffectiveRate().setScale(4, RoundingMode.HALF_UP);
        rateUsed = bank.getEffectiveRate().setScale(4, RoundingMode.HALF_UP);
        break;

      default:
        throw new IllegalArgumentException("Tipo de tasa no permitido");
    }

    BigDecimal discountRate = calculateDiscountRate(calculatedOrUsedTEP).setScale(4, RoundingMode.HALF_UP);
    BigDecimal receivedValue = calculateReceivedValue(nominalValue, discountRate).setScale(4, RoundingMode.HALF_UP);

    // Crear la compra
    PurchaseEntity purchase = PurchaseEntity.builder()
        .purchaseDate(document.getDiscountDate())
        .currency(bank.getMainCurrency())
        .nominalValue(nominalValue)
        .discountRate(discountRate)
        .receivedValue(receivedValue)
        .days(days)
        .tep(calculatedOrUsedTEP)
        .rateType(purchaseDTO.getRateType())
        .rateValue(rateUsed)
        .state(StateEnum.PENDING)
        .bank(bank)
        .document(document)
        .build();

    document.setState(StateEnum.PENDING);

    // Guardar la compra y asociar el documento
    documentRepository.save(document);
    purchaseRepository.save(purchase);

    return new ResponseDTO("Venta registrada con éxito");
  }

  @Override
  public PurchaseEquationsDTO getPurchaseCalculations(String username, RegisterPurchaseDTO purchaseDTO) {
    // Buscar la empresa
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

    // Buscar el documento
    DocumentEntity document = documentRepository.findByIdAndCompanyId(purchaseDTO.getDocumentId(), company.getId())
        .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));

    if (document.getState() == StateEnum.PENDING || document.getState() == StateEnum.PAID) {
      throw new IllegalArgumentException("El documento ya fue vendido");
    }

    BankEntity bank = bankRepository.findById(purchaseDTO.getBankId())
        .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado"));

    // Calcular el valor nominal en la moneda del banco (conversión si es necesario)
    BigDecimal nominalValue = document.getAmount();
    if (document.getCurrency() != bank.getMainCurrency()) {
      ExchangeRateDTO exchangeRateDTO = this.purchaseUtils.getTodayExchangeRate();
      nominalValue = this.purchaseUtils.convertCurrency(nominalValue, document.getCurrency(), bank.getMainCurrency(),
          exchangeRateDTO);
    }
    nominalValue = nominalValue.setScale(4, RoundingMode.HALF_UP); // Limitar a 4 decimales

    // Calcular los días entre la fecha de descuento y la fecha de expiración del
    // documento
    Integer days = (int) ChronoUnit.DAYS.between(document.getDiscountDate(), document.getExpirationDate());

    // Calcular TEP y la tasa nominal o efectiva utilizada
    BigDecimal calculatedOrUsedTEP = null;
    BigDecimal rateUsed = null;

    switch (purchaseDTO.getRateType()) {
      case NOMINAL:
        calculatedOrUsedTEP = calculateTep(bank.getNominalRate(), days).setScale(4, RoundingMode.HALF_UP);
        rateUsed = bank.getNominalRate().setScale(4, RoundingMode.HALF_UP);
        break;
      case EFFECTIVE:
        calculatedOrUsedTEP = bank.getEffectiveRate().setScale(4, RoundingMode.HALF_UP);
        rateUsed = bank.getEffectiveRate().setScale(4, RoundingMode.HALF_UP);
        break;
      default:
        throw new IllegalArgumentException("Tipo de tasa no permitido");
    }

    // Calcular la tasa de descuento
    BigDecimal discountRate = calculateDiscountRate(calculatedOrUsedTEP).setScale(4, RoundingMode.HALF_UP);

    // Calcular el valor recibido después del descuento
    BigDecimal receivedValueAmount = calculateReceivedValue(nominalValue, discountRate).setScale(4,
        RoundingMode.HALF_UP);

    // Multiplicar por 100 y limitar a 4 decimales
    Tep tep = Tep.builder()
        .tn(rateUsed)
        .m(360)
        .n(days)
        .value(calculatedOrUsedTEP.multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP))
        .build();

    DiscountedRate discountedRate = DiscountedRate.builder()
        .tep(calculatedOrUsedTEP)
        .value(discountRate.multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP))
        .build();

    ReceivedValue receivedValue = ReceivedValue.builder()
        .nominalValue(nominalValue)
        .d(discountRate)
        .value(receivedValueAmount.setScale(4, RoundingMode.HALF_UP))
        .build();

    return PurchaseEquationsDTO.builder()
        .tep(tep)
        .discountedRate(discountedRate)
        .receivedValue(receivedValue)
        .build();
  }

  private BigDecimal calculateTep(BigDecimal nominalRate, Integer n) {
    // TEP = ((1 + TN/m)^n) - 1
    double m = 360;
    return BigDecimal.valueOf(Math.pow(1 + nominalRate.doubleValue() / m, n) - 1);
  }

  public BigDecimal calculateDiscountRate(BigDecimal tep) {
    // d = TEP / (1 + TEP)
    BigDecimal denominator = BigDecimal.ONE.add(tep);
    BigDecimal discountRate = tep.divide(denominator, RoundingMode.HALF_UP);
    return discountRate;
  }

  private BigDecimal calculateReceivedValue(BigDecimal nominalValue, BigDecimal discountRate) {
    // Valor recibido = Valor nominal * (1 - tasa de descuento)
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
    log.info("- Monto pagado al banco (VR que recibió la empresa): " + purchase.getReceivedValue());
    log.info("- Valor nominal del documento (Total recuperado del cliente): " + purchase.getNominalValue());
    log.info("- Fecha de pago: " + LocalDateTime.now());

    // Ganancia del banco
    BigDecimal nominalValue = purchase.getNominalValue(); // Total recuperado del cliente
    BigDecimal receivedValue = purchase.getReceivedValue(); // Valor pagado a la empresa
    CurrencyEnum purchaseCurrency = purchase.getCurrency();
    CompanyEntity company = purchase.getDocument().getCompany();
    CurrencyEnum mainCurrency = company.getMainCurrency();

    // Conversión de moneda si es necesario
    BigDecimal convertedReceivedValue = receivedValue;
    if (!purchaseCurrency.equals(mainCurrency)) {
      ExchangeRateDTO exchangeRateDTO = this.purchaseUtils.getTodayExchangeRate();
      convertedReceivedValue = this.purchaseUtils.convertCurrency(receivedValue, purchaseCurrency, mainCurrency,
          exchangeRateDTO);
    }

    // Diferencia entre lo recibido del cliente y lo pagado a la empresa
    BigDecimal bankProfit = nominalValue.subtract(receivedValue);
    log.info("- Ganancia del banco en esta operación: " + bankProfit + " " + purchaseCurrency);

    // Actualización de balances
    bank.setBalance(bank.getBalance().add(bankProfit));
    company.setBalance(company.getBalance().add(convertedReceivedValue));

    // Cambiar estados
    purchase.getDocument().setState(StateEnum.PAID);
    purchase.setPayDate(LocalDate.now());
    purchase.setState(StateEnum.PAID);

    // Guardar cambios
    bankRepository.save(bank);
    companyRepository.save(company);
    documentRepository.save(purchase.getDocument());
    purchaseRepository.save(purchase);

    // Actualización del WebSocket con los datos actualizados del dashboard
    CompanyDashboard updatedDashboard = companyService.getDashboard(company.getUsername(),
        company.getPreviewDataCurrency());
    this.companyUpdateService.sendDashboardUpdate(updatedDashboard);

    return new ResponseDTO("Pago realizado con éxito!");
  }

  @Override
  public void tryToBuyByPurchaseDate() {
    List<PurchaseEntity> purchases = purchaseRepository.findAllByState(StateEnum.PENDING);

    log.info("Cantidad de compras pendientes de pago: " + purchases.size());

    // Realiza el pago si la fecha de descuento es igual o anterior a la fecha
    // actual
    for (PurchaseEntity purchase : purchases) {
      if (purchase.getDocument().getDiscountDate() != null &&
          !purchase.getDocument().getDiscountDate().isAfter(LocalDate.now())) {
        this.payDocument(purchase.getBank().getUsername(), purchase.getId());
      }
    }
  }
}
