package xyz.brianuceda.sserafimflow.dtos.purchase;

import xyz.brianuceda.sserafimflow.enums.RateTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterPurchaseDTO {
  private Long bankId;
  private Long documentId;
  private RateTypeEnum rateType;
}
