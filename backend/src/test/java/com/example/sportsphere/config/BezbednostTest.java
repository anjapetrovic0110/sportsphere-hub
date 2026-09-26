package com.example.sportsphere.config;

import com.example.sportsphere.db.KorisnikRepo;
import com.example.sportsphere.models.*;
import com.example.sportsphere.services.JwtServis;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.config.import=", "DB_PASSWORD=test-only",
    "JWT_SECRET=test-only-signing-key-at-least-32-characters-long",
    "CORS_ALLOWED_ORIGIN=https://frontend.example"})
@AutoConfigureMockMvc
class BezbednostTest {
  @Autowired MockMvc mvc;
  @Autowired JwtServis jwt;
  @Autowired PasswordEncoder encoder;
  @MockitoBean KorisnikRepo korisnici;
  Korisnik korisnik;

  @BeforeEach void setup() {
    korisnik = new Korisnik();
    korisnik.setKorisnickoIme("ana"); korisnik.setUloga(Uloga.ATHLETE);
    korisnik.setStatus(StatusKorisnika.ACTIVE);
    korisnik.setHesLozinke(encoder.encode("DemoPass1!"));
    when(korisnici.pronadjiPoKorisnickomImenu("ana")).thenReturn(Optional.of(korisnik));
  }
  @Test void validLoginIssuesToken() throws Exception {
    mvc.perform(post("/prijava").contentType("application/json")
        .content("{\"korisnickoIme\":\"ana\",\"lozinka\":\"DemoPass1!\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("token").isString());
  }
  @Test void wrongPasswordCannotLogin() throws Exception {
    mvc.perform(post("/prijava").contentType("application/json")
        .content("{\"korisnickoIme\":\"ana\",\"lozinka\":\"wrong\"}"))
        .andExpect(status().isBadRequest());
  }
  @Test void athleteCannotUseEmployeeOrAdminEndpoints() throws Exception {
    String token = jwt.napraviToken(korisnik);
    mvc.perform(get("/sportista/status").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
    mvc.perform(get("/zaposleni/status").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
    mvc.perform(get("/admin/korisnici").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }
  @Test void disabledAccountAndInvalidTokenAreDenied() throws Exception {
    String token = jwt.napraviToken(korisnik);
    korisnik.setStatus(StatusKorisnika.DISABLED);
    mvc.perform(get("/sportista/status").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
    mvc.perform(get("/sportista/status").header("Authorization", "Bearer invalid"))
        .andExpect(status().isForbidden());
  }
  @Test void corsUsesConfiguredOriginIncludingPreflight() throws Exception {
    mvc.perform(options("/sportista/profil").header("Origin", "https://frontend.example")
        .header("Access-Control-Request-Method", "POST")
        .header("Access-Control-Request-Headers", "authorization,content-type"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "https://frontend.example"));
    mvc.perform(options("/prijava").header("Origin", "https://untrusted.example")
        .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isForbidden());
  }
  @Test void missingNewPasswordIsRejectedBeforeDatabaseAccess() throws Exception {
    mvc.perform(post("/javno/lozinka/reset").contentType("application/json")
        .content("{\"token\":\"anything\"}"))
        .andExpect(status().isBadRequest());
  }
}
