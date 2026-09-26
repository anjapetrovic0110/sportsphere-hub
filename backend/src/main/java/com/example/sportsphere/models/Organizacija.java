package com.example.sportsphere.models;

import java.util.ArrayList;
import java.util.List;

public class Organizacija {
  private Long id;

  private String naziv;

  private String registrovanaAdresa;

  private String maticniBroj;

  private String poreskiBroj;

  private List<Korisnik> zaposleni = new ArrayList<>();

  public Organizacija() {}

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
  public String getRegistrovanaAdresa() {
    return registrovanaAdresa;
  }
  public void setRegistrovanaAdresa(String registrovanaAdresa) {
    this.registrovanaAdresa = registrovanaAdresa;
  }
  public String getMaticniBroj() {
    return maticniBroj;
  }
  public void setMaticniBroj(String maticniBroj) {
    this.maticniBroj = maticniBroj;
  }
  public String getPoreskiBroj() {
    return poreskiBroj;
  }
  public void setPoreskiBroj(String poreskiBroj) {
    this.poreskiBroj = poreskiBroj;
  }
  public List<Korisnik> getZaposleni() {
    return zaposleni;
  }
  public void setZaposleni(List<Korisnik> zaposleni) {
    this.zaposleni = zaposleni;
  }
}
