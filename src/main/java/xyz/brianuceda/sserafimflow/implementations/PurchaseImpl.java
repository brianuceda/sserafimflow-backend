package xyz.brianuceda.sserafimflow.implementations;

import java.util.List;

import xyz.brianuceda.sserafimflow.dtos.ResponseDTO;
import xyz.brianuceda.sserafimflow.dtos.purchase.PurchaseEquationsDTO;
import xyz.brianuceda.sserafimflow.dtos.purchase.PurchasedDocumentDTO;
import xyz.brianuceda.sserafimflow.dtos.purchase.RegisterPurchaseDTO;
import xyz.brianuceda.sserafimflow.enums.AuthRoleEnum;
import xyz.brianuceda.sserafimflow.enums.StateEnum;

public interface PurchaseImpl {
  PurchaseEquationsDTO getPurchaseCalculations(String username, RegisterPurchaseDTO purchaseDTO);
  ResponseDTO sellDocument(String username, RegisterPurchaseDTO purchaseDTO);

  List<PurchasedDocumentDTO> getPurchasesBySpecificState(String username, AuthRoleEnum role, StateEnum state);
  ResponseDTO payDocument(String username, Long purchaseId);
  
  void tryToBuyByPurchaseDate();
}
