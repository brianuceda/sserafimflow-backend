package com.brianuceda.sserafimflow.dtos.portfolio;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatePortfolioDTO {
  private String name;
  private List<Long> documentIds;
}
