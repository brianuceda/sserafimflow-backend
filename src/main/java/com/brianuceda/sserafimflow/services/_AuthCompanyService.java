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

import com.brianuceda.sserafimflow.dtos.CompanyDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.entities.CompanyEntity;
import com.brianuceda.sserafimflow.enums.AuthRoleEnum;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.brianuceda.sserafimflow.implementations.AwsS3Impl;
import com.brianuceda.sserafimflow.implementations._AuthCompanyImpl;
import com.brianuceda.sserafimflow.respositories.CompanyRepository;
import com.brianuceda.sserafimflow.utils.JwtUtils;

import jakarta.transaction.Transactional;

@Service
public class _AuthCompanyService implements _AuthCompanyImpl {
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;
  private final CompanyRepository companyRepository;
  private final AwsS3Impl _awsS3Impl;

  public _AuthCompanyService(
      AuthenticationManager authenticationManager,
      PasswordEncoder passwordEncoder,
      JwtUtils jwtUtils,
      CompanyRepository companyRepository,
      AwsS3Impl _awsS3Impl) {
    
    this.authenticationManager = authenticationManager;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtils = jwtUtils;
    this.companyRepository = companyRepository;
    this._awsS3Impl = _awsS3Impl;
  }

  @Override
  @Transactional
  public ResponseDTO register(CompanyDTO companyDTO, MultipartFile image, Boolean rememberMe) {
    if (companyRepository.findByUsername(companyDTO.getUsername()).isPresent()) {
      throw new BadCredentialsException("La empresa ya existe");
    }

    if (image != null) {
      companyDTO.setImageUrl(_awsS3Impl.uploadFile(image, companyDTO.getUsername()));
    }

    CompanyEntity company = CompanyEntity.builder()
        .realName(companyDTO.getRealName())
        .ruc(companyDTO.getRuc())
        .username(companyDTO.getUsername())
        .password(passwordEncoder.encode(companyDTO.getPassword()))
        .imageUrl(companyDTO.getImageUrl() != null ? companyDTO.getImageUrl() : "https://i.ibb.co/BrwL76K/company.png")
        .mainCurrency(companyDTO.getMainCurrency() != null ? companyDTO.getMainCurrency() : CurrencyEnum.PEN)
        .previewDataCurrency(companyDTO.getMainCurrency() != null ? companyDTO.getMainCurrency() : CurrencyEnum.PEN)
        .balance(BigDecimal.valueOf(0.0))
        .role(AuthRoleEnum.COMPANY)
        .creationDate(companyDTO.getCreationDate() != null ? (LocalDate) companyDTO.getCreationDate() : LocalDate.now())
        .accountCreationDate(Timestamp.from(Instant.now()))
        .build();

    companyRepository.save(company);

    Map<String, Object> extraClaims = new HashMap<String, Object>();
    extraClaims.put("realName", company.getRealName());
    extraClaims.put("image", company.getImageUrl());
    extraClaims.put("role", AuthRoleEnum.COMPANY.name());

    return new ResponseDTO(null, jwtUtils.genToken(company, extraClaims, rememberMe));
  }

  @Override
  @Transactional
  public ResponseDTO login(CompanyDTO companyDTO, Boolean rememberMe) {
    authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(companyDTO.getUsername(), companyDTO.getPassword()));
    CompanyEntity company = companyRepository.findByUsername(companyDTO.getUsername()).get();

    Map<String, Object> extraClaims = new HashMap<String, Object>();
    extraClaims.put("realName", company.getRealName());
    extraClaims.put("image", company.getImageUrl());
    extraClaims.put("role", AuthRoleEnum.COMPANY.name());

    return new ResponseDTO(null, jwtUtils.genToken(company, extraClaims, rememberMe));
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
