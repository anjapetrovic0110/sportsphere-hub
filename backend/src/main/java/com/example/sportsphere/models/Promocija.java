package com.example.sportsphere.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Promocija {
  private Long id;
  private Objekat objekat;
  private String naziv;
  private LocalDate datumPocetka;
  private LocalDate datumZavrsetka;
  private BigDecimal procenatPopusta;
  private BigDecimal iznosPopusta;
  private Sport sport;
  private boolean aktivan;

  public Promocija() {}
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
  public String getNaziv() {
    return naziv;
  }
  public void setNaziv(String naziv) {
    this.naziv = naziv;
  }
  public LocalDate getDatumPocetka() {
    return datumPocetka;
  }
  public void setDatumPocetka(LocalDate datumPocetka) {
    this.datumPocetka = datumPocetka;
  }
  public LocalDate getDatumZavrsetka() {
    return datumZavrsetka;
  }
  public void setDatumZavrsetka(LocalDate datumZavrsetka) {
    this.datumZavrsetka = datumZavrsetka;
  }
  public BigDecimal getProcenatPopusta() {
    return procenatPopusta;
  }
  public void setProcenatPopusta(BigDecimal value) {
    procenatPopusta = value;
  }
  public BigDecimal getIznosPopusta() {
    return iznosPopusta;
  }
  public void setIznosPopusta(BigDecimal value) {
    iznosPopusta = value;
  }
  public Sport getSport() {
    return sport;
  }
  public void setSport(Sport sport) {
    this.sport = sport;
  }
  public boolean jeAktivan() {
    return aktivan;
  }
  public void setAktivan(boolean aktivan) {
    this.aktivan = aktivan;
  }
}
