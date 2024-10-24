package com.brianuceda.sserafimflow.implementations;

import java.util.List;

import com.brianuceda.sserafimflow.dtos.PurchasedDocumentDTO;
import com.brianuceda.sserafimflow.dtos.RegisterPurchaseDTO;
import com.brianuceda.sserafimflow.dtos.ResponseDTO;
import com.brianuceda.sserafimflow.enums.AuthRoleEnum;
import com.brianuceda.sserafimflow.enums.StateEnum;

public interface PurchaseImpl {
  ResponseDTO sellDocument(String username, RegisterPurchaseDTO purchaseDTO);
  List<PurchasedDocumentDTO> getPurchasesBySpecificState(String username, AuthRoleEnum role, StateEnum state);
  ResponseDTO payDocument(String username, Long purchaseId);
  
  void tryToBuyByPurchaseDate();
}