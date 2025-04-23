package xyz.brianuceda.sserafimflow.implementations;

import java.util.List;

import xyz.brianuceda.sserafimflow.dtos.DocumentDTO;
import xyz.brianuceda.sserafimflow.dtos.ResponseDTO;
import xyz.brianuceda.sserafimflow.enums.StateEnum;

public interface DocumentImpl {
  List<DocumentDTO> getAllDocumentsBySpecificState(String username, StateEnum state);
  List<DocumentDTO> getAllDocumentsNotInAnyPortfolio(String username);
  List<DocumentDTO> getAllDocumentsExceptingPortfolioId(String username, Long portfolioId);
  DocumentDTO getDocumentById(String username, Long documentId);
  ResponseDTO deleteDocument(String username, Long documentId);
  ResponseDTO createDocument(String username, DocumentDTO documentDTO);
  ResponseDTO updateDocument(String username, DocumentDTO updatedFields);
}
