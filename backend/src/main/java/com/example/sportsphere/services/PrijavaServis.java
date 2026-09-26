package com.example.sportsphere.services;

import com.example.sportsphere.db.KorisnikRepo;
import com.example.sportsphere.models.Korisnik;
import com.example.sportsphere.models.StatusKorisnika;
import com.example.sportsphere.models.helpers.PrijavaOdgovor;
import com.example.sportsphere.models.helpers.PrijavaZahtev;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PrijavaServis {
  private final KorisnikRepo korisnikRepozitorijum;
  private final PasswordEncoder enkoderLozinke;
  private final JwtServis jwtServis;

  public PrijavaServis(
      KorisnikRepo korisnikRepozitorijum, PasswordEncoder enkoderLozinke, JwtServis jwtServis) {
    this.korisnikRepozitorijum = korisnikRepozitorijum;
    this.enkoderLozinke = enkoderLozinke;
    this.jwtServis = jwtServis;
  }

  public PrijavaOdgovor prijava(PrijavaZahtev zahtev) {
    Korisnik korisnik =
        korisnikRepozitorijum.pronadjiPoKorisnickomImenu(zahtev.getKorisnickoIme().trim())
            .orElseThrow(() -> new IllegalArgumentException("Pogrešno korisničko ime ili lozinka"));
    if (!enkoderLozinke.matches(zahtev.getLozinka(), korisnik.getHesLozinke()))
      throw new IllegalArgumentException("Pogrešno korisničko ime ili lozinka");
    if (korisnik.getStatus() == StatusKorisnika.PENDING)
      throw new IllegalArgumentException("Zahtev za registraciju još nije odobren");
    if (korisnik.getStatus() != StatusKorisnika.ACTIVE)
      throw new IllegalArgumentException("Nalog nije aktivan");
    return new PrijavaOdgovor(jwtServis.napraviToken(korisnik), korisnik.getKorisnickoIme(),
        korisnik.getIme(), korisnik.getPrezime(), korisnik.getUloga().name());
  }
}
