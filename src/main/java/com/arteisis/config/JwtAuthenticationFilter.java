package com.arteisis.config;

import com.arteisis.model.entity.Role;
import com.arteisis.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if (path == null || path.isEmpty()) {
            path = request.getRequestURI();
            String context = request.getContextPath();
            if (context != null && !context.isEmpty() && path.startsWith(context)) {
                path = path.substring(context.length());
            }
        }

        boolean isPublic = path.startsWith("/api/catalog")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || "/swagger-ui.html".equals(path)
                || ("/api/auth/login".equals(path) && "POST".equalsIgnoreCase(request.getMethod()))
                || ("/api/auth/register".equals(path) && "POST".equalsIgnoreCase(request.getMethod()))
                || "/error".equals(path);

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = header.substring(7).trim();
            if (!token.isEmpty()) {
                try {
                    Claims claims = jwtService.parseClaims(token);
                    String email = claims.getSubject();
                    String roleName = claims.get("role", String.class);
                    if (roleName == null || roleName.isBlank()) {
                        roleName = Role.CUSTOMER.name();
                    }
                    var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName));
                    var authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (JwtException | IllegalArgumentException ex) {
                    if (!isPublic) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
