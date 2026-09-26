package com.example.sportsphere.models.helpers;

import com.example.sportsphere.models.Korisnik;

public class RegistracijaPregled {
  private Long id;
  private String korisnickoIme;
  private String ime;
  private String prezime;
  private String telefon;
  private String email;
  private String uloga;
  private String slika;

  public RegistracijaPregled(Korisnik korisnik) {
    id = korisnik.getId();
    korisnickoIme = korisnik.getKorisnickoIme();
    ime = korisnik.getIme();
    prezime = korisnik.getPrezime();
    telefon = korisnik.getTelefon();
    email = korisnik.getEmail();
    uloga = korisnik.getUloga().name();
    slika = korisnik.getPutanjaProfilneSlike();
  }

  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getKorisnickoIme() {
    return korisnickoIme;
  }
  public void setKorisnickoIme(String korisnickoIme) {
    this.korisnickoIme = korisnickoIme;
  }
  public String getIme() {
    return ime;
  }
  public void setIme(String ime) {
    this.ime = ime;
  }
  public String getPrezime() {
    return prezime;
  }
  public void setPrezime(String prezime) {
    this.prezime = prezime;
  }
  public String getTelefon() {
    return telefon;
  }
  public void setTelefon(String telefon) {
    this.telefon = telefon;
  }
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }
  public String getUloga() {
    return uloga;
  }
  public void setUloga(String uloga) {
    this.uloga = uloga;
  }
  public String getSlika() {
    return slika;
  }
  public void setSlika(String slika) {
    this.slika = slika;
  }
}
