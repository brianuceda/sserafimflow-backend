package xyz.brianuceda.sserafimflow.implementations;

import java.math.BigDecimal;
import java.util.List;

import xyz.brianuceda.sserafimflow.dtos.BankDTO;
import xyz.brianuceda.sserafimflow.dtos.ResponseDTO;

public interface BankImpl {
  BankDTO getProfile(String username);
  ResponseDTO addMoney(String username, BigDecimal amount);
  List<BankDTO> getAllBanks();
}
