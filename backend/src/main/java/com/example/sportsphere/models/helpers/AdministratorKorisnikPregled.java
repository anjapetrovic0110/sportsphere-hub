package com.example.sportsphere.models.helpers;
import com.example.sportsphere.models.Korisnik;
public class AdministratorKorisnikPregled {
  private Long id;
  private String korisnickoIme, ime, prezime, telefon, email, uloga, status;
  public AdministratorKorisnikPregled(Korisnik k) {
    id = k.getId();
    korisnickoIme = k.getKorisnickoIme();
    ime = k.getIme();
    prezime = k.getPrezime();
    telefon = k.getTelefon();
    email = k.getEmail();
    uloga = k.getUloga().name();
    status = k.getStatus().name();
  }
  public Long getId() {
    return id;
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
  public String getUloga() {
    return uloga;
  }
  public String getStatus() {
    return status;
  }
}
