package com.example.sportsphere.models.helpers;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public class NoviOglasZahtev {
  @NotNull(message= "Sport je obavezan") private Long identifikatorSporta;

  @NotBlank(message= "Grad je obavezan")
  @Size(max = 100, message= "Grad može imati najviše 100 karaktera")
  private String grad;

  @NotNull(message= "Datum je obavezan") private LocalDate datumIgre;

  @NotNull(message= "Vreme je obavezno") private LocalTime vremeIgre;

  @Min(value = 1, message= "Broj igrača mora biti najmanje 1")
  @Max(value = 50, message= "Broj igrača može biti najviše 50")
  private int nedostajuciIgraci;

  public Long getIdentifikatorSporta() {
    return identifikatorSporta;
  }
  public void setIdentifikatorSporta(Long identifikatorSporta) {
    this.identifikatorSporta = identifikatorSporta;
  }
  public String getGrad() {
    return grad;
  }
  public void setGrad(String grad) {
    this.grad = grad;
  }
  public LocalDate getDatumIgre() {
    return datumIgre;
  }
  public void setDatumIgre(LocalDate datumIgre) {
    this.datumIgre = datumIgre;
  }
  public LocalTime getVremeIgre() {
    return vremeIgre;
  }
  public void setVremeIgre(LocalTime vremeIgre) {
    this.vremeIgre = vremeIgre;
  }
  public int getNedostajuciIgraci() {
    return nedostajuciIgraci;
  }
  public void setNedostajuciIgraci(int nedostajuciIgraci) {
    this.nedostajuciIgraci = nedostajuciIgraci;
  }
}
