package com.example.sportsphere.models;

import java.math.BigDecimal;

public class Oprema {
  private Long id;
  private Organizacija organizacija;
  private Sport sport;
  private String naziv;
  private String putanjaSlike;
  private BigDecimal cena;
  private Integer zaliha;
  private boolean aktivan = true;

  public Oprema() {}
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public Organizacija getOrganizacija() {
    return organizacija;
  }
  public void setOrganizacija(Organizacija value) {
    organizacija = value;
  }
  public Sport getSport() {
    return sport;
  }
  public void setSport(Sport value) {
    sport = value;
  }
  public String getNaziv() {
    return naziv;
  }
  public void setNaziv(String value) {
    naziv = value;
  }
  public String getPutanjaSlike() {
    return putanjaSlike;
  }
  public void setPutanjaSlike(String value) {
    putanjaSlike = value;
  }
  public BigDecimal getCena() {
    return cena;
  }
  public void setCena(BigDecimal value) {
    cena = value;
  }
  public Integer getZaliha() {
    return zaliha;
  }
  public void setZaliha(Integer value) {
    zaliha = value;
  }
  public boolean jeAktivan() {
    return aktivan;
  }
  public void setAktivan(boolean value) {
    aktivan = value;
  }
}
