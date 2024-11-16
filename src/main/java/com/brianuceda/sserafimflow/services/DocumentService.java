package com.brianuceda.sserafimflow.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import lombok.extern.java.Log;

@Service
@Log
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
  public List<DocumentDTO> getAllDocumentsExceptingPortfolioId(String username, Long portfolioId) {
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
    
    List<DocumentEntity> documents = company.getDocuments();

    documents = documents.stream()
        .filter(document -> 
            document.getPortfolio() == null || // No pertenece a ningún portafolio
            document.getPortfolio().getId().equals(portfolioId) // Pertenece al portafolio especificado
        )
        .collect(Collectors.toList());

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
  public DocumentDTO getDocumentById(String username, Long documentId) {
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

    DocumentEntity document = documentRepository.findById(documentId)
        .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));

    // Verificar que el documento pertenece a la empresa
    if (!document.getCompany().getId().equals(company.getId())) {
      throw new IllegalArgumentException("Recurso protegido");
    }

    return new DocumentDTO(document);
  }

  @Override
  @Transactional
  public ResponseDTO deleteDocument(String username, Long documentId) {
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

    DocumentEntity document = documentRepository.findById(documentId)
        .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));

    if (!document.getCompany().getId().equals(company.getId())) {
      throw new IllegalArgumentException("Recurso protegido");
    }

    if (document.getState().equals(StateEnum.PENDING) || document.getState().equals(StateEnum.PAID)) {
      throw new IllegalArgumentException("No se puede eliminar un documento vendido");
    }

    documentRepository.delete(document);
    return new ResponseDTO("Documento eliminado con éxito");
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

  @Override
  @Transactional
  public ResponseDTO updateDocument(String username, DocumentDTO updatedFields) {
    // Buscar la compañía por el nombre de usuario
    CompanyEntity company = companyRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

    // Buscar el documento a editar
    DocumentEntity document = documentRepository.findById(updatedFields.getId())
        .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));

    // Verificar que el documento pertenece a la empresa
    if (!document.getCompany().getId().equals(company.getId())) {
      throw new IllegalArgumentException("No tiene permiso para editar este documento");
    }

    if (document.getState().equals(StateEnum.PENDING) || document.getState().equals(StateEnum.PAID)) {
      throw new IllegalArgumentException("No se puede editar un documento vendido");
    }

    // Lista de campos actualizados
    List<String> fieldsUpdated = new ArrayList<>();

    // Actualizar solo los campos permitidos
    if (updatedFields.getDocumentType() != null &&
        !updatedFields.getDocumentType().equals(document.getDocumentType())) {
      document.setDocumentType(updatedFields.getDocumentType());
      fieldsUpdated.add("documentType");
    }

    if (updatedFields.getAmount() != null &&
        updatedFields.getAmount().compareTo(BigDecimal.ZERO) > 0 &&
        !updatedFields.getAmount().equals(document.getAmount())) {
      document.setAmount(updatedFields.getAmount());
      fieldsUpdated.add("amount");
    }

    if (updatedFields.getCurrency() != null &&
        !updatedFields.getCurrency().equals(document.getCurrency())) {
      document.setCurrency(updatedFields.getCurrency());
      fieldsUpdated.add("currency");
    }

    if (updatedFields.getDiscountDate() != null &&
        !updatedFields.getDiscountDate().isBefore(LocalDate.now()) &&
        !updatedFields.getDiscountDate().equals(document.getDiscountDate())) {

      document.setDiscountDate(updatedFields.getDiscountDate());
      fieldsUpdated.add("discountDate");
    }

    if (updatedFields.getExpirationDate() != null &&
        !updatedFields.getExpirationDate().isBefore(LocalDate.now()) &&
        (updatedFields.getDiscountDate() == null 
            || updatedFields.getExpirationDate().isAfter(updatedFields.getDiscountDate())) &&
        !updatedFields.getExpirationDate().equals(document.getExpirationDate())) {

      document.setExpirationDate(updatedFields.getExpirationDate());
      fieldsUpdated.add("expirationDate");
    }

    if (updatedFields.getClientName() != null &&
        !updatedFields.getClientName().trim().isEmpty() &&
        !updatedFields.getClientName().equals(document.getClientName())) {
      document.setClientName(updatedFields.getClientName());
      fieldsUpdated.add("clientName");
    }

    if (updatedFields.getClientPhone() != null &&
        !updatedFields.getClientPhone().trim().isEmpty() &&
        !updatedFields.getClientPhone().equals(document.getClientPhone())) {
      document.setClientPhone(updatedFields.getClientPhone());
      fieldsUpdated.add("clientPhone");
    }

    // Guardar cambios en el repositorio
    documentRepository.save(document);

    // Retornar respuesta
    return new ResponseDTO("Documento " + document.getId() + " actualizado con éxito");
  }
}
