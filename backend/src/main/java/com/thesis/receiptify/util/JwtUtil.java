package com.thesis.receiptify.util;

import com.thesis.receiptify.model.AuthResponse;
import com.thesis.receiptify.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.function.Function;


@Service
public class JwtUtil {
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private TokenRepository tokenRepository;

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration:86400000}") // Default to 24 hours (in milliseconds)
    private long JWT_EXPIRATION_TIME;

    /**
     * Extracts the username from a JWT token.
     *
     * @param token The JWT token
     * @return The username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token The JWT token
     * @return The expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts the roles/authorities from a JWT token.
     *
     * @param token The JWT token
     * @return List of granted authorities
     */
    public List<GrantedAuthority> extractRole(String token) {
        final Claims claims = extractAllClaims(token);
        return (List<GrantedAuthority>) claims.get("auth");
    }

    /**
     * Extracts a specific claim from a JWT token.
     *
     * @param <T> The type of the claim
     * @param token The JWT token
     * @param claimsResolver Function to extract the claim
     * @return The extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a JWT token.
     *
     * @param token The JWT token
     * @return All claims
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    /**
     * Checks if a JWT token is expired.
     *
     * @param token The JWT token
     * @return true if the token is expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        try {
            final Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Generates a JWT token for a user.
     *
     * @param userDetails The user details
     * @return The generated JWT token
     */
    public String generateToken(UserDetails userDetails) {
        return createToken(userDetails);
    }

    /**
     * Creates a JWT token with claims.
     *
     * @param userDetails The user details
     * @return The created JWT token
     */
    private String createToken(UserDetails userDetails) {
        // Current time
        Date now = new Date();

        // Expiration time
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION_TIME);

        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("auth", userDetails.getAuthorities())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        // Save token in database
        AuthResponse authResponse = new AuthResponse(token, userDetails.getAuthorities().toString());
        tokenRepository.save(authResponse);

        return token;
    }

    /**
     * Validates a JWT token.
     * Checks if the token is for the correct user, not expired, and exists in the database.
     *
     * @param token The JWT token
     * @param userDetails The user details
     * @return true if the token is valid, false otherwise
     */
    @Cacheable(value = "tokenCache", key = "#token")
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) &&
                    !isTokenExpired(token) &&
                    tokenRepository.existsAuthResponseByToken(token) &&
                    containsAuth(token, userDetails));
        } catch (Exception e) {
            // Any exception during validation means the token is invalid
            return false;
        }
    }

    /**
     * Checks if a token contains the correct authorities for a user.
     *
     * @param token The JWT token
     * @param userDetails The user details
     * @return true if the token contains the correct authorities, false otherwise
     */
    public boolean containsAuth(String token, UserDetails userDetails) {
        AuthResponse authResponse = tokenRepository.findAuthResponseByToken(token);
        if (authResponse == null) {
            return false;
        }
        return userDetails.getAuthorities().toString().contains(
                authResponse.getRole().substring(1, authResponse.getRole().length() - 1));
    }

    /**
     * Invalidates a JWT token by removing it from the database and cache.
     *
     * @param token The JWT token
     */
    public void inValidateToken(String token) {
        AuthResponse authResponse = tokenRepository.findAuthResponseByTokenContains(token.substring(7));
        if (authResponse != null) {
            tokenRepository.delete(authResponse);
            if (cacheManager.getCache("tokenCache") != null) {
                cacheManager.getCache("tokenCache").evict(token);
            }
        }
    }

    /**
     * Scheduled task to clean up expired tokens from the database
     * Runs once per day
     */
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // Once per day
    @Transactional
    public void cleanupExpiredTokens() {
        List<AuthResponse> allTokens = tokenRepository.findAll();
        Date now = new Date();

        for (AuthResponse authResponse : allTokens) {
            try {
                String token = authResponse.getToken();
                Claims claims = Jwts.parser()
                        .setSigningKey(SECRET_KEY)
                        .parseClaimsJws(token)
                        .getBody();

                Date expiration = claims.getExpiration();
                if (expiration != null && expiration.before(now)) {
                    tokenRepository.delete(authResponse);
                    if (cacheManager.getCache("tokenCache") != null) {
                        cacheManager.getCache("tokenCache").evict(token);
                    }
                }
            } catch (Exception e) {
                // Token is invalid or expired, remove it
                tokenRepository.delete(authResponse);
            }
        }
    }
}

