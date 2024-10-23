package com.brianuceda.sserafimflow.services;

import org.springframework.stereotype.Service;

import com.brianuceda.sserafimflow.dtos.CompanyDTO;
import com.brianuceda.sserafimflow.entities.CompanyEntity;
import com.brianuceda.sserafimflow.implementations.CompanyImpl;
import com.brianuceda.sserafimflow.respositories.CompanyRepository;

@Service
public class CompanyService implements CompanyImpl {
  private final CompanyRepository companyRepository;

  public CompanyService(CompanyRepository companyRepository) {
    this.companyRepository = companyRepository;
  }

  @Override
  public CompanyDTO getProfile(String username) {
    CompanyEntity bank = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado"));

    return new CompanyDTO(bank);
  }
}
