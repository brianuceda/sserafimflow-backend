package com.brianuceda.sserafimflow.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import com.brianuceda.sserafimflow.dtos.DocumentDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.entities.CompanyEntity;
import com.brianuceda.sserafimflow.entities.DocumentEntity;
import com.brianuceda.sserafimflow.enums.StateEnum;
import com.brianuceda.sserafimflow.implementations.DocumentImpl;
import com.brianuceda.sserafimflow.respositories.CompanyRepository;
import com.brianuceda.sserafimflow.respositories.DocumentRepository;

import jakarta.transaction.Transactional;

@Service
public class DocumentService implements DocumentImpl {

  private final CompanyRepository companyRepository;
  private final DocumentRepository documentRepository;

  public DocumentService(
      CompanyRepository companyRepository,
      DocumentRepository documentRepository) {

    this.companyRepository = companyRepository;
    this.documentRepository = documentRepository;
  }

  @Override
  public List<DocumentDTO> getAllDocumentsBySpecificState(String username, StateEnum state) {
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

    // Obtener los documentos de la empresa según el estado
    List<DocumentEntity> documents = company.getDocuments();

    if (state != null) {
      documents.removeIf(document -> !document.getState().equals(state));
    }

    // Convertir a DTO y retornar
    return this.convertEntityListToDTOList(documents);
  }

  @Override
  public List<DocumentDTO> getAllDocumentsNotInAnyPortfolio(String username) {
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

    List<DocumentEntity> documents = company.getDocuments();

    // Filtrar los documentos que no están en ninguna cartera
    documents.removeIf(document -> document.getPortfolio() != null);

    // Convertir a DTO y retornar
    return this.convertEntityListToDTOList(documents);
  }

  private List<DocumentDTO> convertEntityListToDTOList(List<DocumentEntity> documents) {
    // Si no hay documentos, retornar lista vacía
    if (documents.isEmpty()) {
      return List.of();
    }

    // Ordenar por ID
    documents.sort((b1, b2) -> b1.getId().compareTo(b2.getId()));

    // Convertir a DTO cada documento
    List<DocumentDTO> documentsDTO = new ArrayList<>();
    for (DocumentEntity document : documents) {
      DocumentDTO documentDTO = new DocumentDTO(document);
      documentDTO.addPortfolioIfExists(document.getPortfolio());
      documentsDTO.add(documentDTO);
    }
    return documentsDTO;
  }

  @Override
  @Transactional
  public ResponseDTO createDocument(String username, DocumentDTO documentDTO) {

    // Buscar la compañía
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

    // Crear el documento
    DocumentEntity document = new DocumentEntity(documentDTO);
    document.setCompany(company);

    // Valores predeterminados
    if (document.getIssueDate() == null) {
      document.setIssueDate(LocalDate.now());
    }
    document.setState(StateEnum.NOT_SELLED);

    // Si se incluye un portfolio, buscarlo
    // if (documentDTO.getPortfolio() != null &&
    // documentDTO.getPortfolio().getName() != null) {
    // PortfolioEntity portfolio =
    // portfolioRepository.findByName(documentDTO.getPortfolio().getName())
    // .orElseThrow(() -> new IllegalArgumentException("Cartera no encontrada"));
    // document.setPortfolio(portfolio);
    // }

    // Guardar el documento
    documentRepository.save(document);
    return new ResponseDTO("Documento registrado con éxito");
  }
}
