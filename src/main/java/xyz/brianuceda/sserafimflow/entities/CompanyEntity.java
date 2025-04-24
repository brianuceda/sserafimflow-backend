package xyz.brianuceda.sserafimflow.entities;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import xyz.brianuceda.sserafimflow.enums.AuthRoleEnum;
import xyz.brianuceda.sserafimflow.enums.CurrencyEnum;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "company")
public class CompanyEntity implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String realName;

  @Column(unique = true, nullable = false, length = 11)
  private String ruc;

  @Column(unique = true, nullable = false, length = 150)
  private String username; // Email

  @Column(nullable = false, length = 255)
  private String password;

  @Column(unique = true, nullable = false, length = 36)
  private String publicUuid;

  @Column(nullable = true)
  private String imageUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CurrencyEnum mainCurrency;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CurrencyEnum previewDataCurrency;

  @Column(precision = 16, scale = 4, nullable = false)
  @Positive
  private BigDecimal balance;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AuthRoleEnum role;

  @Column(nullable = false)
  private LocalDate creationDate;

  @Column(nullable = false)
  private Timestamp accountCreationDate;

  @OneToMany(mappedBy = "company", fetch = FetchType.EAGER)
  private List<DocumentEntity> documents;

  @OneToMany(mappedBy = "company", fetch = FetchType.EAGER)
  private List<PortfolioEntity> portfolios;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Set.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
  
  @Override
  public String toString() {
    return "CompanyEntity(id=" + id + 
           ", realName=" + realName + 
           ", ruc=" + ruc + 
           ", username=" + username + 
           ", publicUuid=" + publicUuid + 
           ", imageUrl=" + imageUrl + 
           ", mainCurrency=" + mainCurrency + 
           ", previewDataCurrency=" + previewDataCurrency + 
           ", balance=" + balance + 
           ", role=" + role + 
           ", creationDate=" + creationDate + 
           ", accountCreationDate=" + accountCreationDate + ")";
  }
}
