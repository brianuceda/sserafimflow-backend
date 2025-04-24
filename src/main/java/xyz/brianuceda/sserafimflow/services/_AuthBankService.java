package xyz.brianuceda.sserafimflow.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import xyz.brianuceda.sserafimflow.dtos.BankDTO;
import xyz.brianuceda.sserafimflow.dtos.ResponseDTO;
import xyz.brianuceda.sserafimflow.entities.BankEntity;
import xyz.brianuceda.sserafimflow.enums.AuthRoleEnum;
import xyz.brianuceda.sserafimflow.enums.CurrencyEnum;
import xyz.brianuceda.sserafimflow.implementations.CloudStorageImpl;
import xyz.brianuceda.sserafimflow.implementations._AuthBankImpl;
import xyz.brianuceda.sserafimflow.respositories.BankRepository;
import xyz.brianuceda.sserafimflow.utils.JwtUtils;

import jakarta.transaction.Transactional;

@Service
public class _AuthBankService implements _AuthBankImpl {
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;
  private final BankRepository bankRepository;
  private final CloudStorageImpl cloudStorageService;

  public _AuthBankService(
      AuthenticationManager authenticationManager,
      PasswordEncoder passwordEncoder,
      JwtUtils jwtUtils,
      BankRepository bankRepository,
      CloudStorageImpl cloudStorageService) {

    this.authenticationManager = authenticationManager;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtils = jwtUtils;
    this.bankRepository = bankRepository;
    this.cloudStorageService = cloudStorageService;
  }

  @Override
  @Transactional
  public ResponseDTO register(BankDTO bankDTO, MultipartFile image, Boolean rememberMe) {
    try {
      if (bankRepository.findByUsername(bankDTO.getUsername()).isPresent()) {
        throw new BadCredentialsException("El banco ya existe");
      }

      String publicUuid = UUID.randomUUID().toString();
      bankDTO.setPublicUuid(publicUuid);

      String imageUrl = null;
      if (image != null) {
        try {
          imageUrl = cloudStorageService.uploadFile(image, publicUuid);
          bankDTO.setImageUrl(imageUrl);
        } catch (Exception e) {
          throw new IllegalArgumentException("Error al subir la imagen: " + e.getMessage());
        }
      }

      BankEntity bank = BankEntity.builder()
          .realName(bankDTO.getRealName())
          .ruc(bankDTO.getRuc())
          .username(bankDTO.getUsername())
          .password(passwordEncoder.encode(bankDTO.getPassword()))
          .publicUuid(publicUuid)
          .imageUrl(imageUrl != null ? imageUrl : "https://i.ibb.co/BrwL76K/bank.png")
          .mainCurrency(bankDTO.getMainCurrency() != null ? bankDTO.getMainCurrency() : CurrencyEnum.PEN)
          .previewDataCurrency(bankDTO.getMainCurrency() != null ? bankDTO.getMainCurrency() : CurrencyEnum.PEN)
          .balance(new BigDecimal(1000000))
          .role(AuthRoleEnum.BANK)
          .creationDate(bankDTO.getCreationDate() != null ? bankDTO.getCreationDate() : LocalDate.now())
          .accountCreationDate(Timestamp.from(Instant.now()))
          .nominalRate(bankDTO.getNominalRate())
          .effectiveRate(bankDTO.getEffectiveRate())
          .extraCommission(bankDTO.getExtraCommission())
          .build();

      bankRepository.save(bank);

      Map<String, Object> extraClaims = new HashMap<String, Object>();
      extraClaims.put("realName", bank.getRealName());
      extraClaims.put("image", bank.getImageUrl());
      extraClaims.put("role", AuthRoleEnum.BANK.name());

      return new ResponseDTO(null, jwtUtils.genToken(bank, extraClaims, rememberMe));
    } catch (BadCredentialsException e) {
      throw e;
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("Error inesperado: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public ResponseDTO login(BankDTO bankDTO, Boolean rememberMe) {
    authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(bankDTO.getUsername(), bankDTO.getPassword()));
    BankEntity bank = bankRepository.findByUsername(bankDTO.getUsername()).get();

    Map<String, Object> extraClaims = new HashMap<String, Object>();
    extraClaims.put("realName", bank.getRealName());
    extraClaims.put("image", bank.getImageUrl());
    extraClaims.put("role", AuthRoleEnum.BANK.name());

    return new ResponseDTO(null, jwtUtils.genToken(bank, extraClaims, rememberMe));
  }

  @Override
  public ResponseDTO logout(String token) {
    if (!jwtUtils.isTokenBlacklisted(token)) {
      jwtUtils.addTokenToBlacklist(token);
      return new ResponseDTO("Desconectado exitosamente");
    } else {
      throw new BadCredentialsException("No se pudo desconectar");
    }
  }
}
