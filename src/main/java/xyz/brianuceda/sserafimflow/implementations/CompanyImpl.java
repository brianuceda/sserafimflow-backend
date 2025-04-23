package xyz.brianuceda.sserafimflow.implementations;

import xyz.brianuceda.sserafimflow.dtos.CompanyDTO;
import xyz.brianuceda.sserafimflow.dtos.CompanyDashboard;
import xyz.brianuceda.sserafimflow.enums.CurrencyEnum;

public interface CompanyImpl {
  CompanyDashboard getDashboard(String username, CurrencyEnum targetCurrency);
  CompanyDTO getProfile(String username);
  CompanyDTO updateCompanyProfile(String username, CompanyDTO updatedFields);
}
