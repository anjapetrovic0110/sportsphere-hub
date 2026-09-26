package com.example.sportsphere.models.helpers;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
public class NovaRezervacijaZahtev {
  @NotNull private Long identifikatorResursa;
  @NotNull private Long identifikatorSporta;
  @NotNull private LocalDate datum;
  @Min(0) @Max(23) private int satOd;
  @Min(1) @Max(24) private int satDo;
  public Long getIdentifikatorResursa() {
    return identifikatorResursa;
  }
  public void setIdentifikatorResursa(Long v) {
    identifikatorResursa = v;
  }
  public Long getIdentifikatorSporta() {
    return identifikatorSporta;
  }
  public void setIdentifikatorSporta(Long v) {
    identifikatorSporta = v;
  }
  public LocalDate getDatum() {
    return datum;
  }
  public void setDatum(LocalDate v) {
    datum = v;
  }
  public int getSatOd() {
    return satOd;
  }
  public void setSatOd(int v) {
    satOd = v;
  }
  public int getSatDo() {
    return satDo;
  }
  public void setSatDo(int v) {
    satDo = v;
  }
}
