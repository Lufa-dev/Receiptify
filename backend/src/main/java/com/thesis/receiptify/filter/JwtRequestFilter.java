package com.thesis.receiptify.filter;

import com.thesis.receiptify.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Auth header: " + authorizationHeader); // Debug line

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String[] splittedToken = authorizationHeader.split("Bearer ");
            jwt = splittedToken[1];
            try {
                username = jwtUtil.extractUsername(jwt);
                System.out.println("Extracted username: " + username); // Debug line
            } catch (Exception e) {
                System.out.println("Token validation error: " + e.getMessage()); // Debug line
                filterChain.doFilter(request, response);
                return;
            }
        }

        if (jwt == null) {
            System.out.println("No JWT token found"); // Debug line
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                User userDetails = (User) this.userDetailsService.loadUserByUsername(username);
                System.out.println("User loaded: " + userDetails.getUsername()); // Debug line

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("Authentication set in SecurityContext"); // Debug line
                } else {
                    System.out.println("Token validation failed"); // Debug line
                }
            } catch (Exception e) {
                System.out.println("Error during authentication: " + e.getMessage()); // Debug line
            }
        }

        filterChain.doFilter(request, response);
    }
}
