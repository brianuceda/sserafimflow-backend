package com.brianuceda.sserafimflow.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brianuceda.sserafimflow.dtos.CompanyDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.dtos.portfolio.ChangeDocumentsInPortfolioDTO;
import com.brianuceda.sserafimflow.dtos.portfolio.CreatePortfolioDTO;
import com.brianuceda.sserafimflow.dtos.portfolio.PortfolioDTO;
import com.brianuceda.sserafimflow.implementations.PortfolioImpl;
import com.brianuceda.sserafimflow.utils.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;

@Log
@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {
  private final JwtUtils jwtUtils;
  private final PortfolioImpl portfolioImpl;

  public PortfolioController(JwtUtils jwtUtils, PortfolioImpl portfolioImpl) {
    this.jwtUtils = jwtUtils;
    this.portfolioImpl = portfolioImpl;
  }

  @PreAuthorize("hasRole('COMPANY')")
  @GetMapping("/get-all-portfolios")
  public ResponseEntity<?> getAllPortfolios(HttpServletRequest request) {
    try {
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);
      
      return new ResponseEntity<>(this.portfolioImpl.getAllPortfolios(username), HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  // documents-in-portfolio (portfolioId)
  @PreAuthorize("hasRole('COMPANY')")
  @GetMapping("/portfolio-by-id")
  public ResponseEntity<?> getPortfolioById(HttpServletRequest request,
      @RequestParam(required = true) Long portfolioId) {
    
    try {
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);

      return new ResponseEntity<>(portfolioImpl.getPortfolioById(username, portfolioId), HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }
  
  @PreAuthorize("hasRole('COMPANY')")
  @PostMapping("/create-portfolio")
  public ResponseEntity<?> createPortfolio(HttpServletRequest request,
      @RequestBody CreatePortfolioDTO createPortfolioDTO) {
    
    try {
      if (createPortfolioDTO.getName().length() > 100) {
        throw new IllegalArgumentException("El nombre es demasiado largo");
      }

      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);
      
      return new ResponseEntity<>(this.portfolioImpl.createPortfolio(username, createPortfolioDTO), HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @PreAuthorize("hasRole('COMPANY')")
  @PutMapping("/change-documents-of-portfolio")
  public ResponseEntity<?> changeDocumentsOfPortfolio(HttpServletRequest request,
      @RequestBody ChangeDocumentsInPortfolioDTO changesPortfolioDTO) {

    try {
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);
      
      return new ResponseEntity<>(this.portfolioImpl.changeDocumentsOfPortfolio(username, changesPortfolioDTO), HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @PreAuthorize("hasRole('COMPANY')")
  @DeleteMapping("/remove-portfolio")
  public ResponseEntity<?> removePortfolio(HttpServletRequest request,
      @RequestParam(required = true) Long portfolioId) {

    try {
      String token = this.jwtUtils.getTokenFromRequest(request);
      String username = this.jwtUtils.getUsernameFromToken(token);
      
      return new ResponseEntity<>(this.portfolioImpl.removePortfolio(username, portfolioId), HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  @PreAuthorize("hasRole('COMPANY')")
  @PutMapping("/update-portfolio")
  public ResponseEntity<?> updatePortfolio(HttpServletRequest request,
      @RequestBody PortfolioDTO portfolioDTO) {
    try {
      String token = jwtUtils.getTokenFromRequest(request);
      String username = jwtUtils.getUsernameFromToken(token);

      return new ResponseEntity<>(this.portfolioImpl.updatePortfolio(username, portfolioDTO), HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(new ResponseDTO(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }
}
