package com.example.demo;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // COMPLETELY DISABLED SECURITY CHECKS as requested:
        // "Disable Spring Security... Allow all requests"
        // ------------------------------------------------------------------------
        // This was the actual root cause of the 401 Unauthorized errors 
        // on POST/PUT requests since it checked for an Authorization header.
        
        filterChain.doFilter(request, response);
        // ------------------------------------------------------------------------
    }
}
