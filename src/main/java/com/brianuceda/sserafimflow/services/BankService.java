package com.brianuceda.sserafimflow.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.brianuceda.sserafimflow.dtos.BankDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.entities.BankEntity;
import com.brianuceda.sserafimflow.implementations.BankImpl;
import com.brianuceda.sserafimflow.respositories.BankRepository;

import jakarta.transaction.Transactional;

@Service
public class BankService implements BankImpl {
  private final BankRepository bankRepository;

  public BankService(BankRepository bankRepository) {
    this.bankRepository = bankRepository;
  }

  @Override
  public BankDTO getProfile(String username) {
    BankEntity bank = bankRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado"));

    return new BankDTO(bank, false);
  }

  @Override
  @Transactional
  public ResponseDTO addMoney(String username, BigDecimal amount) {
    BankEntity bank = bankRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado"));
    
    bank.setBalance(bank.getBalance().add(amount));
    bankRepository.save(bank);

    return new ResponseDTO("Dinero añadido correctamente");
  }

  @Override
  public List<BankDTO> getAllBanks() {
    List<BankEntity> banks = bankRepository.findAll();
    
    if (banks.isEmpty()) {
      return List.of();
    }
    
    // Ordenar por ID
    banks.sort((b1, b2) -> b1.getId().compareTo(b2.getId()));

    // Convertir a DTO
    List<BankDTO> banksDTOs = new ArrayList<>();
    // List<BankDTO> banksDTOs = banks.stream().map(bank -> new BankDTO(bank, true)).toList();

    for (BankEntity bank : banks) {
      BankDTO bankDTO = new BankDTO(bank, true);
      bankDTO.setUsername(null);
      bankDTO.setPassword(null);
      bankDTO.setBalance(null);
      banksDTOs.add(bankDTO);
    }

    return banksDTOs;
  }
    
}
