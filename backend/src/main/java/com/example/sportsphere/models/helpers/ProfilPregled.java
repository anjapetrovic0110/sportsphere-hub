package com.example.sportsphere.models.helpers;

import com.example.sportsphere.models.Korisnik;
import java.util.List;

public class ProfilPregled {
  private String korisnickoIme;
  private String ime;
  private String prezime;
  private String telefon;
  private String email;
  private String urlProfilneSlike;
  private List<Long> identifikatoriOmiljenihSportova;

  public ProfilPregled(Korisnik korisnik, String urlProfilneSlike) {
    korisnickoIme = korisnik.getKorisnickoIme();
    ime = korisnik.getIme();
    prezime = korisnik.getPrezime();
    telefon = korisnik.getTelefon();
    email = korisnik.getEmail();
    this.urlProfilneSlike = urlProfilneSlike;
    identifikatoriOmiljenihSportova = korisnik.getOmiljeniSportovi().stream().map(sport -> sport.getId()).toList();
  }

  public String getKorisnickoIme() {
    return korisnickoIme;
  }
  public String getIme() {
    return ime;
  }
  public String getPrezime() {
    return prezime;
  }
  public String getTelefon() {
    return telefon;
  }
  public String getEmail() {
    return email;
  }
  public String getUrlProfilneSlike() {
    return urlProfilneSlike;
  }
  public List<Long> getIdentifikatoriOmiljenihSportova() {
    return identifikatoriOmiljenihSportova;
  }
}
