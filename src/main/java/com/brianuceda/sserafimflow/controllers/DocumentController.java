package com.brianuceda.sserafimflow.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.brianuceda.sserafimflow.dtos.DocumentDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.enums.StateEnum;
import com.brianuceda.sserafimflow.implementations.DocumentImpl;
import com.brianuceda.sserafimflow.utils.DataUtils;
import com.brianuceda.sserafimflow.utils.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/document")
public class DocumentController {
  private final DocumentImpl documentImpl;
  private final JwtUtils jwtUtils;

  public DocumentController(DocumentImpl documentImpl, JwtUtils jwtUtils) {
    this.documentImpl = documentImpl;
    this.jwtUtils = jwtUtils;
  }

  @PreAuthorize("hasRole('COMPANY')")
  @PostMapping("/create-document")
  public ResponseEntity<ResponseDTO> registerDocument(HttpServletRequest request, @RequestBody DocumentDTO documentDTO) {
    try {
      // Validaciones
      validateDocumentRequest(request, documentDTO);

      // Información del usuario
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);

      return new ResponseEntity<>(documentImpl.createDocument(username, documentDTO), HttpStatus.CREATED);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    } catch (Exception ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }
  
  @PreAuthorize("hasRole('COMPANY')")
  @GetMapping("/documents-by-specific-state")
  public ResponseEntity<?> getAllDocumentsBySpecificState(HttpServletRequest request, @RequestParam(required = false) StateEnum state) {
    try {
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);

      List<DocumentDTO> documents = documentImpl.getAllDocumentsBySpecificState(username, state);

      return new ResponseEntity<>(documents, HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }
  
  @PreAuthorize("hasRole('COMPANY')")
  @GetMapping("/documents-not-in-any-portfolio")
  public ResponseEntity<?> getAllDocumentsNotInAnyPortfolio(HttpServletRequest request, @RequestParam(required = false) StateEnum state) {
    try {
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);

      List<DocumentDTO> documents = documentImpl.getAllDocumentsNotInAnyPortfolio(username);

      return new ResponseEntity<>(documents, HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  private void validateDocumentRequest(HttpServletRequest request, DocumentDTO documentDTO) {
    if (documentDTO.getDocumentType() == null) {
      throw new IllegalArgumentException("El tipo de documento es obligatorio");
    }
    if (documentDTO.getAmount() == null || documentDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("El monto del documento debe ser positivo");
    }
    if (documentDTO.getCurrency() == null) {
      throw new IllegalArgumentException("La moneda es obligatoria");
    }
    if (documentDTO.getDueDate() == null) {
      throw new IllegalArgumentException("La fecha de vencimiento es obligatoria");
    }
    if (documentDTO.getClientName() == null || documentDTO.getClientName().trim().isEmpty()) {
      throw new IllegalArgumentException("El nombre del cliente es obligatorio");
    }
    if (documentDTO.getDueDate().isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("La fecha de vencimiento debe ser futura");
    }

    DataUtils.verifySQLInjection(documentDTO.getClientName());
    DataUtils.verifySQLInjection(documentDTO.getClientPhone());
  }
}
