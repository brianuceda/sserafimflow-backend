package xyz.brianuceda.sserafimflow.interceptors;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import xyz.brianuceda.sserafimflow.enums.AuthRoleEnum;
import xyz.brianuceda.sserafimflow.exceptions.SecurityExceptions.BlacklistedTokenException;
import xyz.brianuceda.sserafimflow.exceptions.SecurityExceptions.ProtectedResource;
import xyz.brianuceda.sserafimflow.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Component
@RequiredArgsConstructor
@Log
@SuppressWarnings("null")
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, java.io.IOException {
        try {
            final String username;
            final String token = jwtUtils.getTokenFromRequest(request);

            // Está iniciando sesión o registrándose
            if (token == null) {
                if (isPublicUrl(request.getRequestURI())) {
                    filterChain.doFilter(request, response);
                    return;
                } else {
                    throw new ProtectedResource("No token provided");
                }
            }

            // Token en blacklist
            if (jwtUtils.isTokenBlacklisted(token)) {
                throw new BlacklistedTokenException("Token is blacklisted");
            }

            username = jwtUtils.getUsernameFromToken(token);

            // Validar si el usuario existe
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtUtils.isValidToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // Continuar con la cadena de filtros
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.severe("Cannot set user authentication: " + e.getMessage());
            throw e;
        }
    }

    // Si la url es pública, retorna true
    private boolean isPublicUrl(String url) {
        return
            url.startsWith("/api/v1/auth") ||
            url.startsWith("/auth") ||
            url.startsWith("/oauth") ||
            url.startsWith("/api/v1/oauth2") ||
            url.startsWith("/login") ||
            url.startsWith("/logout") ||
            // Oauth2
            url.startsWith("/error") ||
            url.contains("/favicon.ico") ||
            // Swagger
            url.startsWith("/v3/api-docs") ||
            url.startsWith("/doc/swagger-ui");
    }
}
