package com.brianuceda.sserafimflow.entities;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.brianuceda.sserafimflow.enums.AuthRoleEnum;
import com.brianuceda.sserafimflow.enums.CurrencyEnum;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bank")
public class BankEntity implements UserDetails {
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

  @Column(nullable = true)
  private String imageUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CurrencyEnum mainCurrency;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CurrencyEnum previewDataCurrency;

  @Column(precision = 16, scale = 4, nullable = false)
  private BigDecimal balance;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AuthRoleEnum role;

  @Column(nullable = false)
  private LocalDate creationDate;

  @Column(nullable = false)
  private Timestamp accountCreationDate;

  @Column(precision = 6, scale = 4, nullable = false)
  @Positive
  private BigDecimal nominalRate;

  @Column(precision = 6, scale = 4, nullable = false)
  @Positive
  private BigDecimal effectiveRate;

  @Column(precision = 16, scale = 4, nullable = true)
  @Positive
  private BigDecimal extraCommission;
  
  @OneToMany(mappedBy = "bank")
  private List<PurchaseEntity> purchases;

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
}
