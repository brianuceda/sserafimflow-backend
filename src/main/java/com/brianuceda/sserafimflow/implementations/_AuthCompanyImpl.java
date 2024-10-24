package com.brianuceda.sserafimflow.implementations;

import org.springframework.web.multipart.MultipartFile;

import com.brianuceda.sserafimflow.dtos.CompanyDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;

public interface _AuthCompanyImpl {
  ResponseDTO register(CompanyDTO companyDTO, MultipartFile image, Boolean rememberMe);
  ResponseDTO login(CompanyDTO companyDTO, Boolean rememberMe);
  ResponseDTO logout(String token);
}
