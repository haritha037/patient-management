package com.pm.authservice.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtUtil {

    private final Key secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret){
        // convert the string key into spring security Key
        // 1. Convert string into bytes
        byte[] keyBytes = Base64.getDecoder()
                .decode(secret.getBytes(StandardCharsets.UTF_8));
        // 2. Convert the bytes into Key object
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, String role){
        return Jwts.builder()
                .subject(email) // a standard field used to store an id that relates to the user
                .claim("role", role) // a custom field that we can add
                .issuedAt(new Date()) // this will also be stored in the token
                .expiration(new Date(System.currentTimeMillis() + 1000 * 3600 * 10)) // 10 hours
                .signWith(secretKey) // encode
                .compact(); // squash everything down to a nice single string
    }


    public void validateToken(String token){
        try {
            Jwts.parser().verifyWith((SecretKey) secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (SignatureException e){
            throw new JwtException("Invalid JWT signature");
        } catch (JwtException e){
            throw new JwtException("Invalid JWT");
        }
    }
}
