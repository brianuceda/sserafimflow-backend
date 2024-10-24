package com.brianuceda.sserafimflow.implementations;

import org.springframework.web.multipart.MultipartFile;

import com.brianuceda.sserafimflow.dtos.BankDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;

public interface _AuthBankImpl {
  ResponseDTO register(BankDTO bankDTO, MultipartFile image, Boolean rememberMe);
  ResponseDTO login(BankDTO bankDTO, Boolean rememberMe);
  ResponseDTO logout(String token);
}
