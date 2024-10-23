package com.brianuceda.sserafimflow.implementations;

import java.math.BigDecimal;
import java.util.List;

import com.brianuceda.sserafimflow.dtos.BankDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;

public interface BankImpl {
  BankDTO getProfile(String username);
  ResponseDTO addMoney(String username, BigDecimal amount);
  List<BankDTO> getAllBanks();
}
