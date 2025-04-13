package com.thesis.receiptify.controller;

import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
            Profile profile = profileService.getProfileByUsername(userDetails.getUsername());
            Map<String, Object> response = new HashMap<>();
            response.put("username", profile.getUsername());
            response.put("firstName", profile.getFirstName());
            response.put("lastName", profile.getLastName());
            response.put("email", profile.getEmail());

            return ResponseEntity.ok(response);
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
            Profile profile = profileService.getProfileByUsername(userDetails.getUsername());

            // Update profile fields if provided
            if (profileData.containsKey("firstName")) {
                profile.setFirstName(profileData.get("firstName"));
            }
            if (profileData.containsKey("lastName")) {
                profile.setLastName(profileData.get("lastName"));
            }
            if (profileData.containsKey("email")) {
                profile.setEmail(profileData.get("email"));
            }

            // Handle password change if requested
            if (profileData.containsKey("currentPassword") && profileData.containsKey("newPassword")) {
                boolean passwordChanged = profileService.changePassword(
                        profile,
                        profileData.get("currentPassword"),
                        profileData.get("newPassword")
                );

                if (!passwordChanged) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Current password is incorrect");
                }
            }

            Profile updatedProfile = profileService.updateProfile(profile);

            Map<String, Object> response = new HashMap<>();
            response.put("username", updatedProfile.getUsername());
            response.put("firstName", updatedProfile.getFirstName());
            response.put("lastName", updatedProfile.getLastName());
            response.put("email", updatedProfile.getEmail());

            return ResponseEntity.ok(response);

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
}
