package com.brianuceda.sserafimflow.implementations;

import java.util.List;

import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.dtos.portfolio.ChangeDocumentsInPortfolioDTO;
import com.brianuceda.sserafimflow.dtos.portfolio.CreatePortfolioDTO;
import com.brianuceda.sserafimflow.dtos.portfolio.PortfolioDTO;

public interface PortfolioImpl {
  List<PortfolioDTO> getAllPortfolios(String username);
  PortfolioDTO getPortfolioById(String username, Long portfolioId);
  ResponseDTO createPortfolio(String username, CreatePortfolioDTO createPortfolioDTO);
  ResponseDTO changeDocumentsOfPortfolio(String username, ChangeDocumentsInPortfolioDTO changesPortfolioDTO);
  ResponseDTO removePortfolio(String username, Long portfolioId);
  ResponseDTO updatePortfolio(String username, PortfolioDTO portfolioDTO);
}
