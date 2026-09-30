package lk.sliit.it3130.farepayment;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JwtTestToken {

    public static void main(String[] args) {

        String secret = System.getenv("JWT_SECRET");

        SecretKey key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        String token = Jwts.builder()
                .subject("test-user")
                .claim("roles", List.of("PASSENGER"))
                .signWith(key)
                .compact();

        System.out.println("\nJWT TOKEN:");
        System.out.println(token);
    }
}