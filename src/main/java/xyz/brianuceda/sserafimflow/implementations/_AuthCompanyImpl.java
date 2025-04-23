package xyz.brianuceda.sserafimflow.implementations;

import org.springframework.web.multipart.MultipartFile;

import xyz.brianuceda.sserafimflow.dtos.CompanyDTO;
import xyz.brianuceda.sserafimflow.dtos.ResponseDTO;

public interface _AuthCompanyImpl {
  ResponseDTO register(CompanyDTO companyDTO, MultipartFile image, Boolean rememberMe);
  ResponseDTO login(CompanyDTO companyDTO, Boolean rememberMe);
  ResponseDTO logout(String token);
}
