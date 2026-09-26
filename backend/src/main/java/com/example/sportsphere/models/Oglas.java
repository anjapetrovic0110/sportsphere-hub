package com.example.sportsphere.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Oglas {
  private Long id;

  private Korisnik vlasnik;

  private Sport sport;

  private String grad;

  private LocalDate datumIgre;

  private LocalTime vremeIgre;

  private Integer nedostajuciIgraci;

  private boolean aktivan = true;

  public Oglas() {}

  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public Korisnik getVlasnik() {
    return vlasnik;
  }
  public void setVlasnik(Korisnik vlasnik) {
    this.vlasnik = vlasnik;
  }
  public Sport getSport() {
    return sport;
  }
  public void setSport(Sport sport) {
    this.sport = sport;
  }
  public String getGrad() {
    return grad;
  }
  public void setGrad(String grad) {
    this.grad = grad;
  }
  public LocalDate getDatumIgre() {
    return datumIgre;
  }
  public void setDatumIgre(LocalDate datumIgre) {
    this.datumIgre = datumIgre;
  }
  public LocalTime getVremeIgre() {
    return vremeIgre;
  }
  public void setVremeIgre(LocalTime vremeIgre) {
    this.vremeIgre = vremeIgre;
  }
  public Integer getNedostajuciIgraci() {
    return nedostajuciIgraci;
  }
  public void setNedostajuciIgraci(Integer nedostajuciIgraci) {
    this.nedostajuciIgraci = nedostajuciIgraci;
  }
  public boolean jeAktivan() {
    return aktivan;
  }
  public void setAktivan(boolean aktivan) {
    this.aktivan = aktivan;
  }
}
