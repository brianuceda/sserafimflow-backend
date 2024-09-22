package com.brianuceda.sserafimflow.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.brianuceda.sserafimflow.dtos.CompanyDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.entities.CompanyEntity;
import com.brianuceda.sserafimflow.enums.RoleEnum;
import com.brianuceda.sserafimflow.implementations.AuthServiceImpl;
import com.brianuceda.sserafimflow.respositories.CompanyRepository;
import com.brianuceda.sserafimflow.utils.JwtUtils;

@Service
public class AuthService implements AuthServiceImpl {
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;
  private final CompanyRepository companyRepository;

  public AuthService(AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, JwtUtils jwtUtils,
      CompanyRepository companyRepository) {
    this.authenticationManager = authenticationManager;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtils = jwtUtils;
    this.companyRepository = companyRepository;
  }

  @Override
  public ResponseDTO register(CompanyDTO companyDTO) {
    if (companyRepository.findByUsername(companyDTO.getEmail()).isPresent()) {
      throw new BadCredentialsException("El usuario ya existe");
    }

    // Buscar si el RUC es real, si es real, obtener el nombre de la empresa
    if (companyDTO.getIsRealRuc()) {
      // Llamar a la API de la SUNAT
      companyDTO.setCompanyName("Empresa de prueba");
    }

    CompanyEntity user = CompanyEntity.builder()
        .companyName(companyDTO.getCompanyName())
        .ruc(companyDTO.getRuc())
        .username(companyDTO.getEmail())
        .password(passwordEncoder.encode(companyDTO.getPassword()))
        .build();

    user.setRole(RoleEnum.COMPANY);

    companyRepository.save(user);

    return new ResponseDTO("Compañía registrada correctamente", jwtUtils.genToken(user));
  }

  @Override
  public ResponseDTO login(CompanyDTO companyDTO) {
    // Autentica al usuario
    authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(companyDTO.getEmail(), companyDTO.getPassword()));

    // Obtiene los detalles del usuario
    UserDetails userDetails = companyRepository.findByUsername(companyDTO.getEmail()).get();

    // Genera el token
    String token = jwtUtils.genToken(userDetails);

    return new ResponseDTO("Sesión iniciada correctamente", token);
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
