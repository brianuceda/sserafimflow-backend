package com.brianuceda.sserafimflow.interceptors;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.brianuceda.sserafimflow.enums.AuthRoleEnum;
import com.brianuceda.sserafimflow.exceptions.SecurityExceptions.BlacklistedTokenException;
import com.brianuceda.sserafimflow.services._CustomUserDetailsService;
import com.brianuceda.sserafimflow.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;

@Component
@Log
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final _CustomUserDetailsService customUserDetailsService;
  private final JwtUtils jwtUtils;

  public JwtAuthenticationFilter(_CustomUserDetailsService customUserDetailsService, JwtUtils jwtUtils) {
    this.customUserDetailsService = customUserDetailsService;
    this.jwtUtils = jwtUtils;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = jwtUtils.getTokenFromRequest(request);

    if (jwtUtils.isTokenBlacklisted(token)) {
      throw new BlacklistedTokenException("Token inválido");
    }

    if (token != null && !token.isEmpty()) {
      String username = jwtUtils.getUsernameFromToken(token);
      AuthRoleEnum role = jwtUtils.getRoleFromToken(token);

      System.out.println("Username: " + username + " Role: " + role);

      if (username != null) {
          UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        if (userDetails != null && jwtUtils.isValidToken(token, userDetails)) {
          var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
    }

    filterChain.doFilter(request, response);
  }
}
