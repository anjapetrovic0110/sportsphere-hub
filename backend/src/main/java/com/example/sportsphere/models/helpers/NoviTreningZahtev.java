package com.example.sportsphere.models.helpers;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class NoviTreningZahtev {
  @NotNull(message= "Trener je obavezan") private Long identifikatorTrenera;

  @NotNull(message= "Datum je obavezan") private LocalDate datum;

  @NotNull(message= "Vreme je obavezno") private LocalTime vreme;

  public Long getIdentifikatorTrenera() {
    return identifikatorTrenera;
  }
  public void setIdentifikatorTrenera(Long identifikatorTrenera) {
    this.identifikatorTrenera = identifikatorTrenera;
  }
  public LocalDate getDatum() {
    return datum;
  }
  public void setDatum(LocalDate datum) {
    this.datum = datum;
  }
  public LocalTime getVreme() {
    return vreme;
  }
  public void setVreme(LocalTime vreme) {
    this.vreme = vreme;
  }
}
