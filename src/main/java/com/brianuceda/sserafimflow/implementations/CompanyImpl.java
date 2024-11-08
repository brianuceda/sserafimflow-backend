package com.brianuceda.sserafimflow.implementations;

import com.brianuceda.sserafimflow.dtos.CompanyDTO;
import com.brianuceda.sserafimflow.dtos.CompanyDashboard;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;

public interface CompanyImpl {
  CompanyDashboard getDashboard(String username, CurrencyEnum targetCurrency);
  CompanyDTO getProfile(String username);
  CompanyDTO updateCompanyProfile(String username, CompanyDTO updatedFields);
}
