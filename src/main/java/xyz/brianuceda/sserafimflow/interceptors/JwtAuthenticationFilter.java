package xyz.brianuceda.sserafimflow.interceptors;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import xyz.brianuceda.sserafimflow.enums.AuthRoleEnum;
import xyz.brianuceda.sserafimflow.exceptions.SecurityExceptions.BlacklistedTokenException;
import xyz.brianuceda.sserafimflow.services._CustomUserDetailsService;
import xyz.brianuceda.sserafimflow.utils.JwtUtils;

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

  @SuppressWarnings("null")
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
    
    try {
        String token = jwtUtils.getTokenFromRequest(request);
        
        if (token != null && !token.isEmpty()) {
            if (jwtUtils.isTokenBlacklisted(token)) {
                throw new BlacklistedTokenException("Token inválido");
            }
            
            String username = jwtUtils.getUsernameFromToken(token);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                
                if (jwtUtils.isValidToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
    } catch (Exception e) {
        logger.error("Cannot set user authentication: {}", e);
    }
    
    filterChain.doFilter(request, response);
  }
}
