package com.example.sportsphere.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Resurs {
  private Long id;
  private Objekat objekat;
  private String naziv;
  private TipResursa tip;
  private Integer kapacitet;
  private String opisOpreme;
  private BigDecimal cenaPoSatu;
  private boolean aktivan = true;
  private List<Sport> sportovi = new ArrayList<>();

  public Resurs() {}

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
  public TipResursa getTip() {
    return tip;
  }
  public void setTip(TipResursa tip) {
    this.tip = tip;
  }
  public Integer getKapacitet() {
    return kapacitet;
  }
  public void setKapacitet(Integer kapacitet) {
    this.kapacitet = kapacitet;
  }
  public String getOpisOpreme() {
    return opisOpreme;
  }
  public void setOpisOpreme(String opisOpreme) {
    this.opisOpreme = opisOpreme;
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
  public List<Sport> getSportovi() {
    return sportovi;
  }
  public void setSportovi(List<Sport> sportovi) {
    this.sportovi = sportovi;
  }
}
