package com.example.sportsphere.models;

import java.time.LocalDateTime;
public class ReakcijaObjekta {
  private Long id;
  private Objekat objekat;
  private Korisnik sportista;
  private TipReakcije reakcija;
  private String komentar;
  private LocalDateTime vremeKreiranja;

  public ReakcijaObjekta() {}
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public Objekat getObjekat() {
    return objekat;
  }
  public void setObjekat(Objekat objekat) {
    this.objekat = objekat;
  }
  public Korisnik getSportista() {
    return sportista;
  }
  public void setSportista(Korisnik sportista) {
    this.sportista = sportista;
  }
  public TipReakcije getReakcija() {
    return reakcija;
  }
  public void setReakcija(TipReakcije reakcija) {
    this.reakcija = reakcija;
  }
  public String getKomentar() {
    return komentar;
  }
  public void setKomentar(String komentar) {
    this.komentar = komentar;
  }
  public LocalDateTime getVremeKreiranja() {
    return vremeKreiranja;
  }
  public void setVremeKreiranja(LocalDateTime vremeKreiranja) {
    this.vremeKreiranja = vremeKreiranja;
  }
}
