package com.brianuceda.sserafimflow.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import com.brianuceda.sserafimflow.dtos.portfolio.PortfolioDTO;
import com.brianuceda.sserafimflow.enums.StateEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "portfolio")
public class PortfolioEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private StateEnum state;

  @ManyToOne
  @JoinColumn(name = "company_id")
  private CompanyEntity company;

  @OneToMany(mappedBy = "portfolio", cascade = CascadeType.PERSIST)
  private List<DocumentEntity> documents;

  public PortfolioEntity(PortfolioDTO portfolioDTO) {
    this.id = portfolioDTO.getId();
    this.name = portfolioDTO.getName();
    this.state = portfolioDTO.getState();
  }
}
