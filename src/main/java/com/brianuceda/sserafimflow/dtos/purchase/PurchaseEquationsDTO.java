package com.brianuceda.sserafimflow.dtos.purchase;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseEquationsDTO {
  private Tep tep;
  private DiscountedRate discountedRate;
  private ReceivedValue receivedValue;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @SuperBuilder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Tep {
    private BigDecimal tn;
    private Integer m;
    private Integer n;
    private BigDecimal value;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @SuperBuilder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class DiscountedRate {
    private BigDecimal tep;
    private BigDecimal value;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @SuperBuilder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ReceivedValue {
    private BigDecimal nominalValue;
    private BigDecimal d;
    private BigDecimal value;
  }
}
