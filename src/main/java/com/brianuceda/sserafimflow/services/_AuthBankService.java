package com.brianuceda.sserafimflow.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.brianuceda.sserafimflow.dtos.BankDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.entities.BankEntity;
import com.brianuceda.sserafimflow.enums.AuthRoleEnum;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.brianuceda.sserafimflow.implementations.AwsS3Impl;
import com.brianuceda.sserafimflow.implementations._AuthBankImpl;
import com.brianuceda.sserafimflow.respositories.BankRepository;
import com.brianuceda.sserafimflow.utils.JwtUtils;

import jakarta.transaction.Transactional;

@Service
public class _AuthBankService implements _AuthBankImpl {
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;
  private final BankRepository bankRepository;
  private final AwsS3Impl _awsS3Impl;

  public _AuthBankService(
      AuthenticationManager authenticationManager,
      PasswordEncoder passwordEncoder,
      JwtUtils jwtUtils,
      BankRepository bankRepository,
      AwsS3Impl _awsS3Impl) {

    this.authenticationManager = authenticationManager;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtils = jwtUtils;
    this.bankRepository = bankRepository;
    this._awsS3Impl = _awsS3Impl;
  }

  @Override
  @Transactional
  public ResponseDTO register(BankDTO bankDTO, MultipartFile image, Boolean rememberMe) {
    if (bankRepository.findByUsername(bankDTO.getUsername()).isPresent()) {
      throw new BadCredentialsException("El banco ya existe");
    }

    if (image != null) {
      bankDTO.setImageUrl(_awsS3Impl.uploadFile(image, bankDTO.getUsername()));
    }

    BankEntity bank = BankEntity.builder()
        .realName(bankDTO.getRealName())
        .ruc(bankDTO.getRuc())
        .username(bankDTO.getUsername())
        .password(passwordEncoder.encode(bankDTO.getPassword()))
        .imageUrl(bankDTO.getImageUrl() != null ? bankDTO.getImageUrl() : "https://i.ibb.co/BrwL76K/bank.png")
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
