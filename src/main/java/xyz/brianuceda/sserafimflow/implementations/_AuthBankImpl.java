package xyz.brianuceda.sserafimflow.implementations;

import org.springframework.web.multipart.MultipartFile;

import xyz.brianuceda.sserafimflow.dtos.BankDTO;
import xyz.brianuceda.sserafimflow.dtos.ResponseDTO;

public interface _AuthBankImpl {
  ResponseDTO register(BankDTO bankDTO, MultipartFile image, Boolean rememberMe);
  ResponseDTO login(BankDTO bankDTO, Boolean rememberMe);
  ResponseDTO logout(String token);
}
