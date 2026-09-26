package com.example.sportsphere.config;

import com.example.sportsphere.db.KorisnikRepo;
import com.example.sportsphere.models.StatusKorisnika;
import com.example.sportsphere.services.JwtServis;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {
  private final JwtServis jwtServis;
  private final KorisnikRepo korisnikRepozitorijum;

  public JwtFilter(JwtServis jwtServis, KorisnikRepo korisnikRepozitorijum) {
    this.jwtServis = jwtServis;
    this.korisnikRepozitorijum = korisnikRepozitorijum;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest zahtev, HttpServletResponse odgovor,
      FilterChain lanac) throws ServletException, IOException {
    String autorizacija = zahtev.getHeader("Authorization");
    if (autorizacija != null && autorizacija.startsWith("Bearer ")) {
      try {
        Claims claims = jwtServis.procitajToken(autorizacija.substring(7));
        String uloga = claims.get("uloga", String.class);
        var korisnik = korisnikRepozitorijum.pronadjiPoKorisnickomImenu(claims.getSubject())
                           .filter(k
                               -> !k.jeObrisan() && k.getStatus() == StatusKorisnika.ACTIVE
                                   && k.getUloga().name().equals(uloga))
                           .orElseThrow();
        UsernamePasswordAuthenticationToken autentikacija = new UsernamePasswordAuthenticationToken(
            korisnik.getKorisnickoIme(), null, List.of(new SimpleGrantedAuthority("ROLE_" + uloga)));
        SecurityContextHolder.getContext().setAuthentication(autentikacija);
      } catch (Exception e) {
        SecurityContextHolder.clearContext();
      }
    }
    lanac.doFilter(zahtev, odgovor);
  }
}
