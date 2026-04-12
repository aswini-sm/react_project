package com.example.demo;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
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

        // Allow CORS preflight requests
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Require token for POST, PUT, DELETE (Let's secure modifying operations)
        // Or you can secure ALL endpoints. Let's secure everything except GET, or everything.
        // For testing add-to-firestore, let's secure POST /students.
        if (request.getMethod().equalsIgnoreCase("POST") || request.getMethod().equalsIgnoreCase("PUT") || request.getMethod().equalsIgnoreCase("DELETE")) {
            String header = request.getHeader("Authorization");

            if (header == null || !header.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Missing or invalid Authorization header");
                return;
            }

            String token = header.substring(7);

            try {
                // Verify the JWT token using Firebase Admin SDK
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                // System.out.println("Valid request from user: " + decodedToken.getEmail());
                
                // Attach user info to request if needed by controllers
                request.setAttribute("userEmail", decodedToken.getEmail());
                request.setAttribute("uid", decodedToken.getUid());

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired Firebase token");
                System.err.println("Token verification failed: " + e.getMessage());
                return;
            }
        }

        // Proceed with the request
        filterChain.doFilter(request, response);
    }
}
