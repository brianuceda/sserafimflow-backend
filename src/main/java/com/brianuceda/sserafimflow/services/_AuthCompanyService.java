package com.brianuceda.sserafimflow.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.brianuceda.sserafimflow.dtos.CompanyDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.entities.CompanyEntity;
import com.brianuceda.sserafimflow.enums.AuthRoleEnum;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;
import com.brianuceda.sserafimflow.implementations._AuthCompanyImpl;
import com.brianuceda.sserafimflow.respositories.CompanyRepository;
import com.brianuceda.sserafimflow.utils.JwtUtils;

@Service
public class _AuthCompanyService implements _AuthCompanyImpl {
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;
  private final CompanyRepository companyRepository;

  public _AuthCompanyService(AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder,
      JwtUtils jwtUtils,
      CompanyRepository companyRepository) {
    this.authenticationManager = authenticationManager;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtils = jwtUtils;
    this.companyRepository = companyRepository;
  }

  @Override
  public ResponseDTO register(CompanyDTO companyDTO) {
    if (companyRepository.findByUsername(companyDTO.getUsername()).isPresent()) {
      throw new BadCredentialsException("La empresa ya existe");
    }

    CompanyEntity company = CompanyEntity.builder()
        .realName(companyDTO.getRealName())
        .ruc(companyDTO.getRuc())
        .username(companyDTO.getUsername())
        .password(passwordEncoder.encode(companyDTO.getPassword()))
        .image(companyDTO.getImage() != null ? companyDTO.getImage() : "https://i.ibb.co/BrwL76K/company.png")
        .currency(CurrencyEnum.PEN)
        .balance(BigDecimal.valueOf(0.0))
        .role(AuthRoleEnum.COMPANY)
        .creationDate(Timestamp.from(Instant.now()))
        .build();

    companyRepository.save(company);

    ResponseDTO response = new ResponseDTO();
    response.setToken(jwtUtils.genToken(company));

    return response;
  }

  @Override
  public ResponseDTO login(CompanyDTO companyDTO) {
    authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(companyDTO.getUsername(), companyDTO.getPassword()));
    UserDetails userDetails = companyRepository.findByUsername(companyDTO.getUsername()).get();
    ResponseDTO response = new ResponseDTO();
    response.setToken(jwtUtils.genToken(userDetails));
    return response;
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
