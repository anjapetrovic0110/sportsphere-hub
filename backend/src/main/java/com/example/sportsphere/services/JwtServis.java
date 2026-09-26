package com.example.sportsphere.services;

import com.example.sportsphere.models.Korisnik;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtServis {
  private final SecretKey kljuc;
  private final long trajanjeMinuta;

  public JwtServis(@Value("${app.jwt.secret}") String tajna,
      @Value("${app.jwt.expiration-minutes:120}") long trajanjeMinuta) {
    this.kljuc = Keys.hmacShaKeyFor(tajna.getBytes(StandardCharsets.UTF_8));
    this.trajanjeMinuta = trajanjeMinuta;
  }

  public String napraviToken(Korisnik korisnik) {
    Instant sada = Instant.now();
    return Jwts.builder()
        .subject(korisnik.getKorisnickoIme())
        .claim("uloga", korisnik.getUloga().name())
        .issuedAt(Date.from(sada))
        .expiration(Date.from(sada.plusSeconds(trajanjeMinuta * 60)))
        .signWith(kljuc)
        .compact();
  }

  public Claims procitajToken(String token) {
    return Jwts.parser().verifyWith(kljuc).build().parseSignedClaims(token).getPayload();
  }
}
