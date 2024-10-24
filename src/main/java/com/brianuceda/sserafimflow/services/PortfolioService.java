package com.brianuceda.sserafimflow.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.dtos.portfolio.ChangeDocumentsInPortfolioDTO;
import com.brianuceda.sserafimflow.dtos.portfolio.CreatePortfolioDTO;
import com.brianuceda.sserafimflow.dtos.portfolio.PortfolioDTO;
import com.brianuceda.sserafimflow.entities.CompanyEntity;
import com.brianuceda.sserafimflow.entities.DocumentEntity;
import com.brianuceda.sserafimflow.entities.PortfolioEntity;
import com.brianuceda.sserafimflow.enums.StateEnum;
import com.brianuceda.sserafimflow.implementations.PortfolioImpl;
import com.brianuceda.sserafimflow.respositories.CompanyRepository;
import com.brianuceda.sserafimflow.respositories.DocumentRepository;
import com.brianuceda.sserafimflow.respositories.PortfolioRepository;

import jakarta.transaction.Transactional;

@Service
public class PortfolioService implements PortfolioImpl {
  private final CompanyRepository companyRepository;
  private final PortfolioRepository portfolioRepository;
  private final DocumentRepository documentRepository;

  public PortfolioService(
      CompanyRepository companyRepository,
      PortfolioRepository portfolioRepository,
      DocumentRepository documentRepository) {

    this.companyRepository = companyRepository;
    this.portfolioRepository = portfolioRepository;
    this.documentRepository = documentRepository;
  }

  @Override
  public List<PortfolioDTO> getAllPortfolios(String username) {
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

    List<PortfolioEntity> portfolios = portfolioRepository.findAllByCompanyId(company.getId());
    return portfolios.stream().map(PortfolioDTO::new).collect(Collectors.toList());
  }

  @Override
  public PortfolioDTO getPortfolioById(String username, Long portfolioId) {
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

    PortfolioEntity portfolio = company.getPortfolios().stream()
        .filter(p -> p.getId().equals(portfolioId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Cartera no encontrada"));

    PortfolioDTO portfolioDTO = new PortfolioDTO(portfolio);
    portfolioDTO.addDocuments(portfolio.getDocuments());

    return portfolioDTO;
  }

  @Override
  @Transactional
  public ResponseDTO createPortfolio(String username, CreatePortfolioDTO createPortfolioDTO) {
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

    PortfolioEntity portfolio = new PortfolioEntity();
    portfolio.setName(createPortfolioDTO.getName());
    portfolio.setState(StateEnum.NOT_SELLED);
    portfolio.setCompany(company);

    portfolioRepository.save(portfolio);

    // Asignar documentos
    if (createPortfolioDTO.getDocumentIds() != null && !createPortfolioDTO.getDocumentIds().isEmpty()) {
      List<DocumentEntity> availableDocuments = company.getDocuments().stream()
          .filter(doc -> doc.getPortfolio() == null)
          .collect(Collectors.toList());

      List<Long> requestedDocumentIds = createPortfolioDTO.getDocumentIds();

      // Verificar disponibilidad de los documentos
      List<DocumentEntity> documentsToAssign = availableDocuments.stream()
          .filter(doc -> requestedDocumentIds.contains(doc.getId()))
          .collect(Collectors.toList());

      if (documentsToAssign.size() != requestedDocumentIds.size()) {
        throw new IllegalArgumentException("Un documento no está disponible");
      }

      // Asignar documentos al portafolio
      for (DocumentEntity document : documentsToAssign) {
        document.setPortfolio(portfolio);
      }

      documentRepository.saveAll(documentsToAssign);
    }

    return new ResponseDTO("Cartera creada con éxito");
  }

  @Override
  @Transactional
  public ResponseDTO changeDocumentsOfPortfolio(String username, ChangeDocumentsInPortfolioDTO changesPortfolioDTO) {
    PortfolioEntity portfolio = this.getPortfolioForCompany(username, changesPortfolioDTO.getPortfolioId());

    // Remover todos los documentos
    List<DocumentEntity> currentDocuments = portfolio.getDocuments();
    for (DocumentEntity doc : currentDocuments) {
      doc.setPortfolio(null);
    }
    documentRepository.saveAll(currentDocuments);

    // Asignar nuevos documentos
    if (changesPortfolioDTO.getDocumentsId() != null && !changesPortfolioDTO.getDocumentsId().isEmpty()) {
      List<DocumentEntity> newDocuments = portfolio.getCompany().getDocuments().stream()
          .filter(doc -> changesPortfolioDTO.getDocumentsId().contains(doc.getId()) && doc.getPortfolio() == null)
          .collect(Collectors.toList());

      if (newDocuments.size() != changesPortfolioDTO.getDocumentsId().size()) {
        throw new IllegalArgumentException("Un documento no está disponible");
      }

      for (DocumentEntity document : newDocuments) {
        document.setPortfolio(portfolio);
      }
      documentRepository.saveAll(newDocuments);
    }

    return new ResponseDTO("Cartera actualizada con éxito");
  }

  @Override
  @Transactional
  public ResponseDTO removePortfolio(String username, Long portfolioId) {
    PortfolioEntity portfolio = this.getPortfolioForCompany(username, portfolioId);

    // Desasociar documentos
    List<DocumentEntity> documents = portfolio.getDocuments();
    for (DocumentEntity document : documents) {
      document.setPortfolio(null);
    }
    documentRepository.saveAll(documents);

    // Eliminar portafolio
    portfolioRepository.delete(portfolio);
    return new ResponseDTO("Cartera eliminada con éxito");
  }

  private PortfolioEntity getPortfolioForCompany(String username, Long portfolioId) {
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

    return company.getPortfolios().stream()
        .filter(p -> p.getId().equals(portfolioId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Cartera no encontrada"));
  }
}