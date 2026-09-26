package com.example.sportsphere.models;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Objekat {
  private Long id;
  private String naziv;
  private String grad;
  private String adresa;
  private String opis;
  private BigDecimal geografskaSirina;
  private BigDecimal geografskaDuzina;
  private Organizacija organizacija;
  private Integer dozvoljeniNedolasci;
  private List<Resurs> resursi = new ArrayList<>();
  private LocalTime vremeOtvaranja;
  private LocalTime vremeZatvaranja;

  private StatusObjekta status;

  public Objekat() {}
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
  public String getGrad() {
    return grad;
  }
  public void setGrad(String grad) {
    this.grad = grad;
  }
  public String getAdresa() {
    return adresa;
  }
  public void setAdresa(String adresa) {
    this.adresa = adresa;
  }
  public String getOpis() {
    return opis;
  }
  public void setOpis(String opis) {
    this.opis = opis;
  }
  public BigDecimal getGeografskaSirina() {
    return geografskaSirina;
  }
  public void setGeografskaSirina(BigDecimal geografskaSirina) {
    this.geografskaSirina = geografskaSirina;
  }
  public BigDecimal getGeografskaDuzina() {
    return geografskaDuzina;
  }
  public void setGeografskaDuzina(BigDecimal geografskaDuzina) {
    this.geografskaDuzina = geografskaDuzina;
  }
  public Organizacija getOrganizacija() {
    return organizacija;
  }
  public void setOrganizacija(Organizacija v) {
    organizacija = v;
  }
  public Integer getDozvoljeniNedolasci() {
    return dozvoljeniNedolasci;
  }
  public void setDozvoljeniNedolasci(Integer v) {
    dozvoljeniNedolasci = v;
  }
  public List<Resurs> getResursi() {
    return resursi;
  }
  public void setResursi(List<Resurs> v) {
    resursi = v;
  }
  public LocalTime getVremeOtvaranja() {
    return vremeOtvaranja;
  }
  public void setVremeOtvaranja(LocalTime vremeOtvaranja) {
    this.vremeOtvaranja = vremeOtvaranja;
  }
  public LocalTime getVremeZatvaranja() {
    return vremeZatvaranja;
  }
  public void setVremeZatvaranja(LocalTime vremeZatvaranja) {
    this.vremeZatvaranja = vremeZatvaranja;
  }
  public StatusObjekta getStatus() {
    return status;
  }
  public void setStatus(StatusObjekta status) {
    this.status = status;
  }
}
