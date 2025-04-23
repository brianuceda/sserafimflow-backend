package xyz.brianuceda.sserafimflow.dtos.portfolio;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangeDocumentsInPortfolioDTO {
  private Long portfolioId;
  private List<Long> documentsId;
}
