package com.example.sportsphere.models.helpers;

public class PrijavaOdgovor {
  private String token;
  private String korisnickoIme;
  private String ime;
  private String prezime;
  private String uloga;

  public PrijavaOdgovor(String token, String korisnickoIme, String ime, String prezime, String uloga) {
    this.token = token;
    this.korisnickoIme = korisnickoIme;
    this.ime = ime;
    this.prezime = prezime;
    this.uloga = uloga;
  }

  public String getToken() {
    return token;
  }
  public void setToken(String token) {
    this.token = token;
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
  public String getUloga() {
    return uloga;
  }
  public void setUloga(String uloga) {
    this.uloga = uloga;
  }
}
