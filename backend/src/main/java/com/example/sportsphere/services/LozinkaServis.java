package com.example.sportsphere.services;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;
import com.example.sportsphere.db.*;
import com.example.sportsphere.models.*;
import com.example.sportsphere.models.helpers.*;
@Service
public class LozinkaServis {
  private final KorisnikRepo korisnici;
  private final TokenZaLozinkuRepo tokeni;
  private final PasswordEncoder enkoder;
  private final JavaMailSender posta;
  private final String frontendUrl;
  private final String posiljalac;
  private static final String PORUKA =
      "Ako aktivan nalog postoji, link za promenu lozinke je poslat na njegovu imejl adresu";
  private final SecureRandom generator = new SecureRandom();
  public LozinkaServis(KorisnikRepo k, TokenZaLozinkuRepo t, PasswordEncoder e,
      JavaMailSender posta, @Value("${app.frontend-url}") String frontendUrl,
      @Value("${app.mail.from}") String posiljalac) {
    korisnici = k;
    tokeni = t;
    enkoder = e;
    this.posta = posta;
    this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    this.posiljalac = posiljalac;
  }
  @Transactional
  public ResetLozinkeOdgovor zahtev(ZahtevZaResetLozinke z) {
    String i = z.getIdentifikator().trim();
    boolean imejl = i.contains("@");
    var korisnik = (imejl ? korisnici.pronadjiPoEmailu(i) : korisnici.pronadjiPoKorisnickomImenu(i))
                       .filter(k -> !k.jeObrisan() && k.getStatus() == StatusKorisnika.ACTIVE)
                       .orElse(null);
    if (korisnik == null)
      return new ResetLozinkeOdgovor(PORUKA);
    tokeni.ponistiAktivne(korisnik.getId());
    byte[] b = new byte[32];
    generator.nextBytes(b);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    TokenZaLozinku t = new TokenZaLozinku();
    t.setKorisnik(korisnik);
    t.setHesTokena(hash(token));
    t.setVremeIsteka(LocalDateTime.now().plusMinutes(30));
    tokeni.sacuvaj(t);
    String link = frontendUrl + "/nova-lozinka?token=" + token;
    SimpleMailMessage poruka = new SimpleMailMessage();
    poruka.setFrom(posiljalac);
    poruka.setTo(korisnik.getEmail());
    poruka.setSubject("SportSphere Hub — promena lozinke");
    poruka.setText("Za promenu lozinke otvorite link (važi 30 minuta):\n" + link
        + "\nAko niste tražili promenu lozinke, zanemarite ovu poruku.");
    posta.send(poruka);
    return new ResetLozinkeOdgovor(PORUKA);
  }
  @Transactional
  public void promeni(NovaLozinkaZahtev z) {
    var t =
        tokeni.zakljucajPoHashu(hash(z.getToken()))
            .orElseThrow(() -> new IllegalArgumentException("Link nije ispravan ili je istekao"));
    if (t.getVremeKoriscenja() != null || !t.getVremeIsteka().isAfter(LocalDateTime.now())
        || t.getKorisnik().jeObrisan() || t.getKorisnik().getStatus() != StatusKorisnika.ACTIVE)
      throw new IllegalArgumentException("Link nije ispravan ili je istekao");
    t.getKorisnik().setHesLozinke(enkoder.encode(z.getLozinka()));
    t.setVremeKoriscenja(LocalDateTime.now());
    tokeni.ponistiAktivne(t.getKorisnik().getId());
    korisnici.sacuvaj(t.getKorisnik());
  }
  private String hash(String token) {
    try {
      byte[] h =
          MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      return java.util.HexFormat.of().formatHex(h);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
