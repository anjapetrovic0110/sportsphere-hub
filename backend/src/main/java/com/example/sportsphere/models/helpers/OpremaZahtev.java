package com.example.sportsphere.models.helpers;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class OpremaZahtev {
  @NotBlank(message= "Naziv opreme je obavezan")
  @Size(max = 160, message= "Naziv opreme može imati najviše 160 karaktera")
  private String naziv;
  @NotNull(message= "Sport je obavezan") private Long identifikatorSporta;
  @NotNull(message= "Cena je obavezna")
  @DecimalMin(value = "0.00", message= "Cena ne može biti negativna")
  private BigDecimal cena;
  @NotNull(message= "Stanje zalihe je obavezno")
  @Min(value = 0, message= "Stanje zalihe ne može biti negativno")
  private Integer zaliha;
  private boolean aktivan = true;

  public String getNaziv() {
    return naziv;
  }
  public void setNaziv(String value) {
    naziv = value;
  }
  public Long getIdentifikatorSporta() {
    return identifikatorSporta;
  }
  public void setIdentifikatorSporta(Long value) {
    identifikatorSporta = value;
  }
  public BigDecimal getCena() {
    return cena;
  }
  public void setCena(BigDecimal value) {
    cena = value;
  }
  public Integer getZaliha() {
    return zaliha;
  }
  public void setZaliha(Integer value) {
    zaliha = value;
  }
  public boolean jeAktivan() {
    return aktivan;
  }
  public void setAktivan(boolean value) {
    aktivan = value;
  }
}
