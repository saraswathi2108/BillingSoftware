package com.billing.software.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.billing.software.security.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // ✅ Token Leka pothey next filter ki velipotham
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Token extract
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        // ✅ User still NOT authenticated in this request
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // ✅ Load user from DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // ✅ Validate token
            if (jwtService.isTokenValid(token, username)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // ✅ Manually Auth set chestunnam
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // ✅ Continue filter chain
        filterChain.doFilter(request, response);
    }
}