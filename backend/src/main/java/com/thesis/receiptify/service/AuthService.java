package com.thesis.receiptify.service;

import com.thesis.receiptify.model.AuthRequest;
import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.RegistrationRequest;
import com.thesis.receiptify.model.enums.Role;
import com.thesis.receiptify.repository.ProfileRepository;
import com.thesis.receiptify.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtTokenUtil;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final CollectionService collectionService;

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
                .roles(Role.USER)
                .build();
        Profile savedProfile = profileRepository.save(profile);

        // Initialize default collections for the new user
        collectionService.initializeDefaultCollections(savedProfile);

        return "OK";

    }

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

    public String generateToken(UserDetails userDetails) {
        return jwtTokenUtil.generateToken(userDetails);
    }

    public void inValidateToken(String token) {
        jwtTokenUtil.inValidateToken(token);
    }

}
