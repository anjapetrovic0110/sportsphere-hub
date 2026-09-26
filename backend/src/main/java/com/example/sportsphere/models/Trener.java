package com.example.sportsphere.models;

import java.math.BigDecimal;

public class Trener {
  private Long id;

  private Objekat objekat;

  private Sport sport;

  private String ime;

  private String prezime;

  private String specijalizacija;

  private BigDecimal prosecnaOcena;

  private BigDecimal cenaPoSatu;

  private boolean aktivan;

  public Trener() {}

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
  public Sport getSport() {
    return sport;
  }
  public void setSport(Sport sport) {
    this.sport = sport;
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
  public String getSpecijalizacija() {
    return specijalizacija;
  }
  public void setSpecijalizacija(String specijalizacija) {
    this.specijalizacija = specijalizacija;
  }
  public BigDecimal getProsecnaOcena() {
    return prosecnaOcena;
  }
  public void setProsecnaOcena(BigDecimal prosecnaOcena) {
    this.prosecnaOcena = prosecnaOcena;
  }
  public BigDecimal getCenaPoSatu() {
    return cenaPoSatu;
  }
  public void setCenaPoSatu(BigDecimal cenaPoSatu) {
    this.cenaPoSatu = cenaPoSatu;
  }
  public boolean jeAktivan() {
    return aktivan;
  }
  public void setAktivan(boolean aktivan) {
    this.aktivan = aktivan;
  }
}
