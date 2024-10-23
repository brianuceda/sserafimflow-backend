package com.brianuceda.sserafimflow.implementations;

import com.brianuceda.sserafimflow.dtos.CompanyDTO;

public interface CompanyImpl {
  CompanyDTO getProfile(String username);
}
