package com.example.sportsphere.models.helpers;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class NoviObjekatZahtev {
  @NotBlank(message= "Naziv objekta je obavezan")
  @Size(max = 160, message= "Naziv objekta može imati najviše 160 karaktera")
  @JsonAlias("name")
  private String naziv;

  @NotBlank(message= "Grad objekta je obavezan")
  @Size(max = 100, message= "Naziv grada može imati najviše 100 karaktera")
  @JsonAlias("city")
  private String grad;

  @NotBlank(message= "Adresa objekta je obavezna")
  @Size(max = 255, message= "Adresa objekta može imati najviše 255 karaktera")
  @JsonAlias("address")
  private String adresa;

  @JsonAlias("description")
  private String opis;
  @JsonAlias("latitude")
  private BigDecimal geografskaSirina;
  @JsonAlias("longitude")
  private BigDecimal geografskaDuzina;

  @NotNull(message= "Vreme otvaranja je obavezno")
  @JsonAlias("openingTime")
  private LocalTime vremeOtvaranja;

  @NotNull(message= "Vreme zatvaranja je obavezno")
  @JsonAlias("closingTime")
  private LocalTime vremeZatvaranja;

  @Min(value = 1, message= "Broj nedolazaka pre zabrane mora biti najmanje 1")
  @JsonAlias("allowedNoShows")
  private int dozvoljeniNedolasci = 3;

  @NotEmpty(message= "Objekat mora imati najmanje jedan resurs")
  @JsonAlias("resources")
  private List<@Valid NoviResursZahtev> resursi = new ArrayList<>();

  public String getNaziv() {
    return naziv;
  }
  public void setNaziv(String v) {
    naziv = v;
  }
  public String getGrad() {
    return grad;
  }
  public void setGrad(String v) {
    grad = v;
  }
  public String getAdresa() {
    return adresa;
  }
  public void setAdresa(String v) {
    adresa = v;
  }
  public String getOpis() {
    return opis;
  }
  public void setOpis(String v) {
    opis = v;
  }
  public BigDecimal getGeografskaSirina() {
    return geografskaSirina;
  }
  public void setGeografskaSirina(BigDecimal v) {
    geografskaSirina = v;
  }
  public BigDecimal getGeografskaDuzina() {
    return geografskaDuzina;
  }
  public void setGeografskaDuzina(BigDecimal v) {
    geografskaDuzina = v;
  }
  public LocalTime getVremeOtvaranja() {
    return vremeOtvaranja;
  }
  public void setVremeOtvaranja(LocalTime v) {
    vremeOtvaranja = v;
  }
  public LocalTime getVremeZatvaranja() {
    return vremeZatvaranja;
  }
  public void setVremeZatvaranja(LocalTime v) {
    vremeZatvaranja = v;
  }
  public int getDozvoljeniNedolasci() {
    return dozvoljeniNedolasci;
  }
  public void setDozvoljeniNedolasci(int v) {
    dozvoljeniNedolasci = v;
  }
  public List<NoviResursZahtev> getResursi() {
    return resursi;
  }
  public void setResursi(List<NoviResursZahtev> v) {
    resursi = v;
  }
}
