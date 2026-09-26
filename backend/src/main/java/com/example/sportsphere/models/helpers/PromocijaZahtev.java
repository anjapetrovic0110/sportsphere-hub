package com.example.sportsphere.models.helpers;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PromocijaZahtev {
  @NotNull(message= "Sportski objekat je obavezan") private Long identifikatorObjekta;
  @NotBlank(message= "Naziv promocije je obavezan")
  @Size(max = 160, message= "Naziv promocije može imati najviše 160 karaktera")
  private String naziv;
  @NotNull(message= "Početni datum je obavezan") private LocalDate datumPocetka;
  @NotNull(message= "Krajnji datum je obavezan") private LocalDate datumZavrsetka;
  @DecimalMin(value = "0.01", message= "Procenat popusta mora biti veći od nule")
  @DecimalMax(value = "100.00", message= "Procenat popusta može biti najviše 100")
  private BigDecimal procenatPopusta;
  @DecimalMin(value = "0.01", message= "Iznos popusta mora biti veći od nule")
  private BigDecimal iznosPopusta;
  private Long identifikatorSporta;
  private boolean aktivan = true;

  public Long getIdentifikatorObjekta() {
    return identifikatorObjekta;
  }
  public void setIdentifikatorObjekta(Long identifikatorObjekta) {
    this.identifikatorObjekta = identifikatorObjekta;
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
  public void setProcenatPopusta(BigDecimal procenatPopusta) {
    this.procenatPopusta = procenatPopusta;
  }
  public BigDecimal getIznosPopusta() {
    return iznosPopusta;
  }
  public void setIznosPopusta(BigDecimal iznosPopusta) {
    this.iznosPopusta = iznosPopusta;
  }
  public Long getIdentifikatorSporta() {
    return identifikatorSporta;
  }
  public void setIdentifikatorSporta(Long identifikatorSporta) {
    this.identifikatorSporta = identifikatorSporta;
  }
  public boolean jeAktivan() {
    return aktivan;
  }
  public void setAktivan(boolean aktivan) {
    this.aktivan = aktivan;
  }
}
