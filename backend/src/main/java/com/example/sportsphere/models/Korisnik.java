package com.example.sportsphere.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Korisnik {
  private Long id;

  private String korisnickoIme;

  private String hesLozinke;

  private String ime;

  private String prezime;

  private String telefon;
  private String email;

  private String putanjaProfilneSlike;

  private Uloga uloga;

  private StatusKorisnika status;
  private boolean obrisan;

  private List<Sport> omiljeniSportovi = new ArrayList<>();

  private LocalDateTime vremeKreiranja;

  private LocalDateTime vremeAzuriranja;

  public Korisnik() {}

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
  public String getHesLozinke() {
    return hesLozinke;
  }
  public void setHesLozinke(String hesLozinke) {
    this.hesLozinke = hesLozinke;
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
  public String getPutanjaProfilneSlike() {
    return putanjaProfilneSlike;
  }
  public void setPutanjaProfilneSlike(String putanjaProfilneSlike) {
    this.putanjaProfilneSlike = putanjaProfilneSlike;
  }
  public Uloga getUloga() {
    return uloga;
  }
  public void setUloga(Uloga uloga) {
    this.uloga = uloga;
  }
  public StatusKorisnika getStatus() {
    return status;
  }
  public void setStatus(StatusKorisnika status) {
    this.status = status;
  }
  public boolean jeObrisan() {
    return obrisan;
  }
  public void setObrisan(boolean obrisan) {
    this.obrisan = obrisan;
  }
  public List<Sport> getOmiljeniSportovi() {
    return omiljeniSportovi;
  }
  public void setOmiljeniSportovi(List<Sport> omiljeniSportovi) {
    this.omiljeniSportovi = omiljeniSportovi;
  }
  public LocalDateTime getVremeKreiranja() {
    return vremeKreiranja;
  }
  public void setVremeKreiranja(LocalDateTime vremeKreiranja) {
    this.vremeKreiranja = vremeKreiranja;
  }
  public LocalDateTime getVremeAzuriranja() {
    return vremeAzuriranja;
  }
  public void setVremeAzuriranja(LocalDateTime vremeAzuriranja) {
    this.vremeAzuriranja = vremeAzuriranja;
  }
}
