package com.brianuceda.sserafimflow.implementations;

import java.util.List;

import com.brianuceda.sserafimflow.dtos.DocumentDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.enums.StateEnum;

public interface DocumentImpl {
  List<DocumentDTO> getAllDocumentsBySpecificState(String username, StateEnum state);
  List<DocumentDTO> getAllDocumentsNotInAnyPortfolio(String username);
  ResponseDTO createDocument(String username, DocumentDTO documentDTO);
}
