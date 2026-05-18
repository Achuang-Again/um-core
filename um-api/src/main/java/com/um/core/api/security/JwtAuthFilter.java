package com.um.core.api.security;

import com.um.core.infrastructure.redis.TokenStoreService;
import com.um.core.infrastructure.security.JwtTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final TokenStoreService tokenStoreService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtTokenService.parse(token);
                if (tokenStoreService.isBlacklisted(claims.getId())) {
                    chain.doFilter(request, response);
                    return;
                }
                Long userId = Long.parseLong(claims.getSubject());
                String deviceId = claims.get("device_id", String.class);
                var principal = new UserPrincipal(userId, deviceId);
                var auth = new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
