package com.example.sportsphere.models;

import java.time.LocalDateTime;

public class TokenZaLozinku {
  private Long id;
  private Korisnik korisnik;
  private String hesTokena;
  private LocalDateTime vremeIsteka;
  private LocalDateTime vremeKoriscenja;
  private LocalDateTime vremeKreiranja;

  public TokenZaLozinku() {}

  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public Korisnik getKorisnik() {
    return korisnik;
  }
  public void setKorisnik(Korisnik korisnik) {
    this.korisnik = korisnik;
  }
  public String getHesTokena() {
    return hesTokena;
  }
  public void setHesTokena(String hesTokena) {
    this.hesTokena = hesTokena;
  }
  public LocalDateTime getVremeIsteka() {
    return vremeIsteka;
  }
  public void setVremeIsteka(LocalDateTime vremeIsteka) {
    this.vremeIsteka = vremeIsteka;
  }
  public LocalDateTime getVremeKoriscenja() {
    return vremeKoriscenja;
  }
  public void setVremeKoriscenja(LocalDateTime vremeKoriscenja) {
    this.vremeKoriscenja = vremeKoriscenja;
  }
  public LocalDateTime getVremeKreiranja() {
    return vremeKreiranja;
  }
  public void setVremeKreiranja(LocalDateTime vremeKreiranja) {
    this.vremeKreiranja = vremeKreiranja;
  }
}
