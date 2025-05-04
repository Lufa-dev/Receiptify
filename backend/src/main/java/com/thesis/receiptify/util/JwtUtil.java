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

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public List<GrantedAuthority> extractRole(String token) {
        final Claims claims = extractAllClaims(token);
        return (List<GrantedAuthority>) claims.get("auth");
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        try {
            final Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public String generateToken(UserDetails userDetails) {
        return createToken(userDetails);
    }

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

    public boolean containsAuth(String token, UserDetails userDetails) {
        AuthResponse authResponse = tokenRepository.findAuthResponseByToken(token);
        if (authResponse == null) {
            return false;
        }
        return userDetails.getAuthorities().toString().contains(
                authResponse.getRole().substring(1, authResponse.getRole().length() - 1));
    }

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

