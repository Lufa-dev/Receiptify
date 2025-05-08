package com.thesis.receiptify.service;

import com.thesis.receiptify.model.Profile;
import com.thesis.receiptify.repository.ProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Service responsible for loading user details for Spring Security.
 * Implements UserDetailsService interface to provide authentication data.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProfileDetailsService implements UserDetailsService {
    private final ProfileRepository profileRepository;

    /**
     * Loads a user by username for authentication.
     * Creates a Spring Security User object from the Profile entity.
     *
     * @param username The username to look up
     * @return UserDetails object for Spring Security
     * @throws UsernameNotFoundException if the user doesn't exist
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Profile user = profileRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User name " + username + " not found"));

        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRoles().name())
        );

        return new User(user.getUsername(), user.getPassword(), authorities);
    }
}
