package com.brianuceda.sserafimflow.implementations;

import com.brianuceda.sserafimflow.dtos.CompanyDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;

public interface AuthServiceImpl {
  ResponseDTO signup(CompanyDTO companyDTO);
  ResponseDTO signin(CompanyDTO companyDTO);
  ResponseDTO logout(String token);
}
