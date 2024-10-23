package com.brianuceda.sserafimflow.implementations;

import com.brianuceda.sserafimflow.dtos.BankDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;

public interface _AuthBankImpl {
  ResponseDTO register(BankDTO bankDTO);
  ResponseDTO login(BankDTO bankDTO);
  ResponseDTO logout(String token);
}
