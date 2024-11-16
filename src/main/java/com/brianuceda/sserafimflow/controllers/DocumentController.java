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
  @PostMapping("/create")
  public ResponseEntity<ResponseDTO> createDocument(HttpServletRequest request, @RequestBody DocumentDTO documentDTO) {
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
  public ResponseEntity<?> getAllDocumentsBySpecificState(HttpServletRequest request,
      @RequestParam(required = false) StateEnum state) {
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
  public ResponseEntity<?> getAllDocumentsNotInAnyPortfolio(HttpServletRequest request,
      @RequestParam(required = false) StateEnum state) {
    try {
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);

      List<DocumentDTO> documents = documentImpl.getAllDocumentsNotInAnyPortfolio(username);

      return new ResponseEntity<>(documents, HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @PreAuthorize("hasRole('COMPANY')")
  @GetMapping("/documents-not-in-portfolio")
  public ResponseEntity<?> getAllDocumentsExceptingPortfolioId(HttpServletRequest request,
      @RequestParam(required = true) Long portfolioId) {
    
    try {
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);

      List<DocumentDTO> documents = documentImpl.getAllDocumentsExceptingPortfolioId(username, portfolioId);

      return new ResponseEntity<>(documents, HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @PreAuthorize("hasRole('COMPANY')")
  @GetMapping("/{id}")
  public ResponseEntity<?> getDocumentById(HttpServletRequest request, @PathVariable Long id) {
    try {
      // Obtener el token y el nombre de usuario
      String token = jwtUtils.getTokenFromRequest(request);
      String username = jwtUtils.getUsernameFromToken(token);

      // Llamar al servicio para obtener el documento
      return new ResponseEntity<>(this.documentImpl.getDocumentById(username, id), HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @PreAuthorize("hasRole('COMPANY')")
  @DeleteMapping("/delete/{documentId}")
  public ResponseEntity<?> deleteDocument(HttpServletRequest request, @PathVariable Long documentId) {
    try {
      // Obtener el token y el nombre de usuario
      String token = jwtUtils.getTokenFromRequest(request);
      String username = jwtUtils.getUsernameFromToken(token);

      // Llamar al servicio para eliminar el documento
      return new ResponseEntity<>(this.documentImpl.deleteDocument(username, documentId), HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @PreAuthorize("hasRole('COMPANY')")
  @PutMapping("/edit")
  public ResponseEntity<?> updateDocument(HttpServletRequest request, @RequestBody DocumentDTO documentDTO) {
    try {
      // Validaciones
      validateDocumentRequest(request, documentDTO);
      
      // Obtener el token y el nombre de usuario
      String token = jwtUtils.getTokenFromRequest(request);
      String username = jwtUtils.getUsernameFromToken(token);

      // Llamar al servicio para actualizar el documento
      return new ResponseEntity<>(this.documentImpl.updateDocument(username, documentDTO), HttpStatus.OK);
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
    if (documentDTO.getDiscountDate() == null) {
      throw new IllegalArgumentException("La fecha de descuento es obligatoria");
    }
    if (documentDTO.getExpirationDate() == null) {
      throw new IllegalArgumentException("La fecha de vencimiento es obligatoria");
    }
    if (documentDTO.getClientName() == null || documentDTO.getClientName().trim().isEmpty()) {
      throw new IllegalArgumentException("El nombre del cliente es obligatorio");
    }
    if (documentDTO.getDiscountDate().isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("La fecha de descuento debe ser futura");
    }
    if (documentDTO.getExpirationDate().isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("La fecha de vencimiento debe ser futura");
    }
    if (documentDTO.getExpirationDate().isBefore(documentDTO.getDiscountDate())) {
      throw new IllegalArgumentException("La fecha de vencimiento debe ser posterior a la fecha de descuento");
    }

    DataUtils.verifySQLInjection(documentDTO.getClientName());
    DataUtils.verifySQLInjection(documentDTO.getClientPhone());
  }
}
