package com.aegira.loan.auth;

import com.aegira.loan.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {
    private final Key key;
    private final long expiration;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(sha256(secret));
        this.expiration = expiration;
    }

    public String generate(User user) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(key, SignatureAlgorithm.HS256).compact();
            
    }

    public UUID getUserId(String token) {
        return UUID.fromString(claims(token).getSubject());
    }

    public boolean valid(String token) {
        claims(token);
        return true;
    }

    private Claims claims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    private byte[] sha256(String secret) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
