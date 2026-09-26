package com.example.sportsphere.models.helpers;

import com.example.sportsphere.db.ResursProjekcija;
import java.math.BigDecimal;

public class ResursPregled {
  private Long id;
  private String naziv;
  private String tip;
  private Integer kapacitet;
  private String oprema;
  private BigDecimal cena;
  private String sportovi;
  public ResursPregled(ResursProjekcija p) {
    id = p.getId();
    naziv = p.getNaziv();
    tip = p.getTip();
    kapacitet = p.getKapacitet();
    oprema = p.getOprema();
    cena = p.getCena();
    sportovi = p.getSportovi();
  }
  public Long getId() {
    return id;
  }
  public String getNaziv() {
    return naziv;
  }
  public String getTip() {
    return tip;
  }
  public Integer getKapacitet() {
    return kapacitet;
  }
  public String getOprema() {
    return oprema;
  }
  public BigDecimal getCena() {
    return cena;
  }
  public String getSportovi() {
    return sportovi;
  }
}
