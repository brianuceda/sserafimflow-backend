package com.brianuceda.sserafimflow.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.brianuceda.sserafimflow.entities.BankEntity;
import com.brianuceda.sserafimflow.entities.CompanyEntity;
import com.brianuceda.sserafimflow.enums.AuthRoleEnum;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtils {
  @Value("${IS_PRODUCTION}")
  private boolean isProduction;
  @Value("${JWT_SECRET_KEY}")
  private String jwtSecretKey;
  @Value("${JWT_EXP_TIME}")
  private String jwtExpTime;

  // Blacklist de Tokens
  private Set<String> memoryBackendBlacklistedTokens = new HashSet<>();

  public String genToken(UserDetails user) {
    Map<String, Object> extraClaims = new HashMap<>();

    if (user instanceof CompanyEntity) {
      extraClaims.put("role", AuthRoleEnum.COMPANY.name());
    } else if (user instanceof BankEntity) {
      extraClaims.put("role", AuthRoleEnum.BANK.name());
    }

    return genToken(extraClaims, user);
  }

  private String genToken(Map<String, Object> extraClaims, UserDetails user) {
    return Jwts.builder()
        .claims(extraClaims) // old: setClaims
        .subject(user.getUsername()) // old: setSubject
        .issuedAt(new Date(System.currentTimeMillis())) // old: setIssuedAt
        .expiration(new Date(System.currentTimeMillis() + Long.parseLong(jwtExpTime))) // old: setExpiration
        .signWith(genTokenSign()) // old: signWith(genTokenSign(), SignatureAlgorithm.HS256)
        .compact();
  }

  private SecretKey genTokenSign() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
    return new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256"); // old: Keys.hmacShaKeyFor(keyBytes);
  }

  private Claims getAllClaims(String token) {
    return Jwts
        .parser() // old: parserBuilder()
        .verifyWith(genTokenSign()) // old: setSigningKey(genTokenSign())
        .build()
        .parseSignedClaims(token) // old: parseClaimsJws(token)
        .getPayload(); // old: getBody()
  }

  private Date getExpiration(String token) {
    return getClaim(token, Claims::getExpiration);
  }

  public String getUsernameFromToken(String token) {
    return getClaim(token, Claims::getSubject);
  }

  public AuthRoleEnum getRoleFromToken(String token) {
    String roleString = getClaim(token, claims -> claims.get("role", String.class));
    try {
      return AuthRoleEnum.valueOf(roleString);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Rol inválido");
    }
  }

  public boolean isValidToken(String token, UserDetails userDetails) {
    final String username = getUsernameFromToken(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  public void addTokenToBlacklist(String token) {
    memoryBackendBlacklistedTokens.add(token);
  }

  private boolean isTokenExpired(String token) {
    if (this.isProduction) {
      return getExpiration(token).before(new Date(System.currentTimeMillis() - 18000000));
    } else {
      return getExpiration(token).before(new Date());
    }
  }

  public boolean isTokenBlacklisted(String token) {
    return memoryBackendBlacklistedTokens.contains(token);
  }

  public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String getUsernameFromBearerToken(String bearerToken) {
    String realToken = bearerToken.replace("Bearer ", "");
    return getUsernameFromToken(realToken);
  }

  public String getTokenFromRequest(HttpServletRequest request) {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    } else {
      return null;
    }
  }

}
