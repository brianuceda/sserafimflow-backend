package com.brianuceda.sserafimflow.filters;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.brianuceda.sserafimflow.exceptions.SecurityExceptions.BlacklistedTokenException;
import com.brianuceda.sserafimflow.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final UserDetailsService userDetailsService;
  private final JwtUtils jwtUtils;

  public JwtAuthenticationFilter(UserDetailsService userDetailsService, JwtUtils jwtUtils) {
    this.userDetailsService = userDetailsService;
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

      if (username != null) {
        var userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtUtils.isValidToken(token, userDetails)) {
          var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
    }

    filterChain.doFilter(request, response);
  }
}
