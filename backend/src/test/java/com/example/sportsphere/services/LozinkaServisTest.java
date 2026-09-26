package com.example.sportsphere.services;

import com.example.sportsphere.db.*;
import com.example.sportsphere.models.*;
import com.example.sportsphere.models.helpers.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LozinkaServisTest {
  private final KorisnikRepo korisnici = mock(KorisnikRepo.class);
  private final TokenZaLozinkuRepo tokeni = mock(TokenZaLozinkuRepo.class);
  private final JavaMailSender posta = mock(JavaMailSender.class);
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  private final LozinkaServis servis = new LozinkaServis(korisnici, tokeni, encoder, posta,
      "https://sportsphere.example/", "noreply@sportsphere.example");
  private Korisnik korisnik;

  @BeforeEach void setup() {
    korisnik = new Korisnik();
    korisnik.setId(1L);
    korisnik.setKorisnickoIme("ana");
    korisnik.setEmail("ana@example.test");
    korisnik.setStatus(StatusKorisnika.ACTIVE);
    when(korisnici.pronadjiPoKorisnickomImenu("ana")).thenReturn(Optional.of(korisnik));
  }

  @Test void deliversOnlyToAccountEmailAndStoresOnlyHash() throws Exception {
    var odgovor = servis.zahtev(zahtev("ana"));
    ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);
    ArgumentCaptor<TokenZaLozinku> saved = ArgumentCaptor.forClass(TokenZaLozinku.class);
    verify(posta).send(mail.capture());
    verify(tokeni).sacuvaj(saved.capture());
    assertArrayEquals(new String[]{"ana@example.test"}, mail.getValue().getTo());
    String body = mail.getValue().getText();
    assertTrue(body.contains("https://sportsphere.example/nova-lozinka?token="));
    String token = body.split("token=")[1].split("\\s")[0];
    assertEquals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
        .digest(token.getBytes(StandardCharsets.UTF_8))), saved.getValue().getHesTokena());
    assertFalse(odgovor.getPoruka().contains(token));
    assertTrue(saved.getValue().getVremeIsteka().isAfter(LocalDateTime.now().plusMinutes(29)));
    verify(tokeni).ponistiAktivne(1L);
  }

  @Test void unknownAndDisabledAccountsHaveSameResponse() {
    String expected = servis.zahtev(zahtev("ana")).getPoruka();
    reset(posta, tokeni);
    assertEquals(expected, servis.zahtev(zahtev("missing")).getPoruka());
    assertEquals(expected, servis.zahtev(zahtev("missing@example.test")).getPoruka());
    korisnik.setStatus(StatusKorisnika.DISABLED);
    assertEquals(expected, servis.zahtev(zahtev("ana")).getPoruka());
    verifyNoInteractions(posta, tokeni);
  }

  @Test void successfulResetHashesPasswordAndConsumesAllTokens() {
    var token = token();
    when(tokeni.zakljucajPoHashu(anyString())).thenReturn(Optional.of(token));
    servis.promeni(novaLozinka());
    assertTrue(encoder.matches("NewPass123!", korisnik.getHesLozinke()));
    assertNotNull(token.getVremeKoriscenja());
    verify(tokeni).ponistiAktivne(1L);
    verify(korisnici).sacuvaj(korisnik);
    assertThrows(IllegalArgumentException.class, () -> servis.promeni(novaLozinka()));
  }

  @Test void expiredTokensCannotChangePassword() {
    var token = token();
    token.setVremeIsteka(LocalDateTime.now().minusMinutes(1));
    when(tokeni.zakljucajPoHashu(anyString())).thenReturn(Optional.of(token));
    assertThrows(IllegalArgumentException.class, () -> servis.promeni(novaLozinka()));
    verify(korisnici, never()).sacuvaj(any());
  }

  private TokenZaLozinku token() {
    var t = new TokenZaLozinku();
    t.setKorisnik(korisnik);
    t.setVremeIsteka(LocalDateTime.now().plusMinutes(30));
    return t;
  }
  private ZahtevZaResetLozinke zahtev(String ime) {
    var z = new ZahtevZaResetLozinke(); z.setIdentifikator(ime); return z;
  }
  private NovaLozinkaZahtev novaLozinka() {
    var z = new NovaLozinkaZahtev(); z.setToken("test-token"); z.setLozinka("NewPass123!"); return z;
  }
}
