package com.brianuceda.sserafimflow.implementations;

import java.util.List;

import com.brianuceda.sserafimflow.dtos.DocumentDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.enums.StateEnum;

public interface DocumentImpl {
  List<DocumentDTO> getAllDocumentsBySpecificState(String username, StateEnum state);
  List<DocumentDTO> getAllDocumentsNotInAnyPortfolio(String username);
  List<DocumentDTO> getAllDocumentsExceptingPortfolioId(String username, Long portfolioId);
  DocumentDTO getDocumentById(String username, Long documentId);
  ResponseDTO deleteDocument(String username, Long documentId);
  ResponseDTO createDocument(String username, DocumentDTO documentDTO);
  ResponseDTO updateDocument(String username, DocumentDTO updatedFields);
}
