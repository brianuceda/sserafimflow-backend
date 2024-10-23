package com.brianuceda.sserafimflow.implementations;

import com.brianuceda.sserafimflow.dtos.CompanyDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;

public interface _AuthCompanyImpl {
  ResponseDTO register(CompanyDTO companyDTO);
  ResponseDTO login(CompanyDTO companyDTO);
  ResponseDTO logout(String token);
}
