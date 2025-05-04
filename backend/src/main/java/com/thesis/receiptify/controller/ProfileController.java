package com.thesis.receiptify.controller;

import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.model.dto.ProfileDTO;
import com.thesis.receiptify.model.enums.IngredientType;
import com.thesis.receiptify.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            ProfileDTO profile = profileService.getUserProfileDTO(userDetails.getUsername());
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve profile: " + e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> profileData,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Handle password change if requested
            if (profileData.containsKey("currentPassword") && profileData.containsKey("newPassword")) {
                boolean passwordChanged = profileService.changePassword(
                        userDetails.getUsername(),
                        profileData.get("currentPassword"),
                        profileData.get("newPassword")
                );

                if (!passwordChanged) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Current password is incorrect");
                }
            }

            ProfileDTO updatedProfile = profileService.updateProfileData(userDetails.getUsername(), profileData);
            return ResponseEntity.ok(updatedProfile);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update profile: " + e.getMessage());
        }
    }

    @GetMapping("/recipe-stats")
    public ResponseEntity<?> getRecipeStats(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Map<String, Object> stats = profileService.getUserRecipeStats(userDetails.getUsername());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve recipe statistics: " + e.getMessage());
        }
    }

    @PutMapping("/preferences")
    public ResponseEntity<?> updatePreferences(
            @RequestBody Map<String, Object> preferences,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            ProfileDTO updatedProfile = profileService.updateUserPreferences(userDetails.getUsername(), preferences);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update preferences: " + e.getMessage());
        }
    }

    @GetMapping("/preferences")
    public ResponseEntity<?> getPreferences(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Map<String, Object> preferences = profileService.getUserPreferences(userDetails.getUsername());
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve preferences: " + e.getMessage());
        }
    }
}

