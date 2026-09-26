package com.example.sportsphere.models;

public class Sport {
  private Long id;

  private String naziv;
  private boolean aktivan;

  public Sport() {}

  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getNaziv() {
    return naziv;
  }
  public void setNaziv(String naziv) {
    this.naziv = naziv;
  }
  public boolean jeAktivan() {
    return aktivan;
  }
  public void setAktivan(boolean aktivan) {
    this.aktivan = aktivan;
  }
}
