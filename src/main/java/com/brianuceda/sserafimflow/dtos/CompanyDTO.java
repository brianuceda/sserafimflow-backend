package com.brianuceda.sserafimflow.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyDTO {
  private String companyName;
  private String ruc;
  private String email; // username
  private String password;
  
  private Boolean isRealRuc;
}
