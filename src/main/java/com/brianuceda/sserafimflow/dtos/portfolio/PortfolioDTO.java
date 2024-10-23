package com.brianuceda.sserafimflow.dtos.portfolio;

import com.brianuceda.sserafimflow.dtos.DocumentDTO;
import com.brianuceda.sserafimflow.entities.DocumentEntity;
import com.brianuceda.sserafimflow.entities.PortfolioEntity;
import com.brianuceda.sserafimflow.enums.StateEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortfolioDTO {
  private Long id;
  private String name;
  private StateEnum state;

  private List<DocumentDTO> documents;

  public PortfolioDTO(PortfolioEntity portfolio) {
    this.id = portfolio.getId();
    this.name = portfolio.getName();
    this.state = portfolio.getState();
  }
  
  public void addDocuments(List<DocumentEntity> documents) {
    this.documents = documents.stream().map(DocumentDTO::new).collect(Collectors.toList());
  }
}
