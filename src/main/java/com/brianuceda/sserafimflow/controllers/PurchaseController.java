package com.brianuceda.sserafimflow.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.dtos.purchase.PurchasedDocumentDTO;
import com.brianuceda.sserafimflow.dtos.purchase.RegisterPurchaseDTO;
import com.brianuceda.sserafimflow.enums.AuthRoleEnum;
import com.brianuceda.sserafimflow.enums.RateTypeEnum;
import com.brianuceda.sserafimflow.enums.StateEnum;
import com.brianuceda.sserafimflow.implementations.PurchaseImpl;
import com.brianuceda.sserafimflow.utils.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1/purchase")
public class PurchaseController {
  private final JwtUtils jwtUtils;
  private final PurchaseImpl purchaseImpl;

  public PurchaseController(JwtUtils jwtUtils, PurchaseImpl purchaseImpl) {
    this.jwtUtils = jwtUtils;
    this.purchaseImpl = purchaseImpl;
  }

  @PreAuthorize("hasRole('COMPANY')")
  @GetMapping("/calculate-purchase")
  public ResponseEntity<?> calculatePurchase(HttpServletRequest request,
      @RequestParam Long bankId, @RequestParam Long documentId, @RequestParam RateTypeEnum rateType) {

    try {
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);

      RegisterPurchaseDTO purchaseDTO = new RegisterPurchaseDTO(bankId, documentId, rateType);

      return new ResponseEntity<>(purchaseImpl.getPurchaseCalculations(username, purchaseDTO), HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @PreAuthorize("hasRole('COMPANY')")
  @PostMapping("/sell-document")
  public ResponseEntity<ResponseDTO> sellDocument(HttpServletRequest request,
      @RequestParam Long bankId, @RequestParam Long documentId, @RequestParam RateTypeEnum rateType) {

    try {
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);

      RegisterPurchaseDTO purchaseDTO = new RegisterPurchaseDTO(bankId, documentId, rateType);

      return new ResponseEntity<>(purchaseImpl.sellDocument(username, purchaseDTO), HttpStatus.CREATED);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @PreAuthorize("hasAnyRole('COMPANY', 'BANK')")
  @GetMapping("/purchases-by-specific-state")
  public ResponseEntity<?> getPurchasesBySpecificState(HttpServletRequest request,
      @RequestParam(required = false) StateEnum state) {

    try {
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);
      AuthRoleEnum role = this.jwtUtils.getRoleFromToken(token);

      List<PurchasedDocumentDTO> purchasedDocuments = purchaseImpl.getPurchasesBySpecificState(username, role, state);

      return new ResponseEntity<>(purchasedDocuments, HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @PreAuthorize("hasRole('BANK')")
  @PostMapping("/pay-purchase")
  public ResponseEntity<ResponseDTO> payDocument(HttpServletRequest request,
      @RequestParam Long purchaseId) {

    try {
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);

      return new ResponseEntity<>(purchaseImpl.payDocument(username, purchaseId), HttpStatus.CREATED);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }
}
