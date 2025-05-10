package com.thesis.receiptify.service;

import com.thesis.receiptify.model.AuthRequest;
import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.RegistrationRequest;
import com.thesis.receiptify.model.enums.Role;
import com.thesis.receiptify.repository.ProfileRepository;
import com.thesis.receiptify.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Service responsible for user authentication and registration operations.
 * Manages token generation, user registration, and authentication validation.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtTokenUtil;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final CollectionService collectionService;
    private final EmailService emailService;

    /**
     * Registers a new user with the provided information.
     * Creates user profile and initializes default collections.
     *
     * @param registrationRequest The registration data
     * @return "OK" if registration is successful
     * @throws IllegalArgumentException if username or email is already taken
     */
    public String register(RegistrationRequest registrationRequest) {
        Optional<Profile> existingUsername = profileRepository.findByUsername(registrationRequest.getUsername());
        if (existingUsername.isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        // Check if email is already in use
        Optional<Profile> existingEmail = profileRepository.findByEmail(registrationRequest.getEmail());
        if (existingEmail.isPresent()) {
            throw new IllegalArgumentException("Email is already in use");
        }

        Profile profile = Profile.builder()
                .email(registrationRequest.getEmail())
                .created(LocalDateTime.now())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .username(registrationRequest.getUsername())
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .roles(Role.USER)
                .build();
        Profile savedProfile = profileRepository.save(profile);

        // Initialize default collections for the new user
        collectionService.initializeDefaultCollections(savedProfile);

        emailService.sendWelcomeEmail(savedProfile);

        return "OK";

    }

    /**
     * Authenticates a user based on credentials and generates a JWT token.
     *
     * @param authRequest The authentication request containing username and password
     * @return JWT token for the authenticated user
     * @throws Exception if authentication fails due to invalid credentials or disabled user
     */
    public String authenticate(AuthRequest authRequest) throws Exception {
        Objects.requireNonNull(authRequest.getUsername());
        Objects.requireNonNull(authRequest.getPassword());
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
            return this.generateToken((UserDetails) authentication.getPrincipal());
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    /**
     * Generates a JWT token for a user.
     *
     * @param userDetails The user details from Spring Security
     * @return JWT token
     */
    public String generateToken(UserDetails userDetails) {
        return jwtTokenUtil.generateToken(userDetails);
    }

    /**
     * Invalidates a JWT token by removing it from the database.
     * Also clears the token from cache if caching is enabled.
     *
     * @param token The JWT token to invalidate
     */
    public void inValidateToken(String token) {
        jwtTokenUtil.inValidateToken(token);
    }

}
