package com.thesis.receiptify.controller;

import com.thesis.receiptify.model.AuthRequest;
import com.thesis.receiptify.model.RegistrationRequest;
import com.thesis.receiptify.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        System.out.println("Register request received for user: " + registrationRequest.getUsername());

        try {
            HttpHeaders headers = new HttpHeaders();
            authService.register(registrationRequest);
            return ResponseEntity.ok().headers(headers).body(registrationRequest.getUsername());
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody AuthRequest authRequest) {
        try {
            HttpHeaders headers = new HttpHeaders();
            String token = authService.authenticate(authRequest);
            headers.add("Authorization", "Bearer " + token);
            return ResponseEntity.ok().headers(headers).body(authRequest.getUsername());
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<String> signoutUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            String[] splittedToken = token.split("Bearer ");
            authService.inValidateToken(splittedToken[1]);
            return new ResponseEntity<>("Signed Out", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Sign out failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
