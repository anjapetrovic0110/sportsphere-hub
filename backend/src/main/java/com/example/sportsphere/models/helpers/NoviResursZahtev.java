package com.example.sportsphere.models.helpers;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class NoviResursZahtev {
  private Long id;

  @NotBlank(message= "Naziv resursa je obavezan")
  @Size(max = 120, message= "Naziv resursa može imati najviše 120 karaktera")
  @JsonAlias("name")
  private String naziv;

  @NotBlank(message= "Tip resursa je obavezan")
  @JsonAlias("type")
  private String tip;

  @NotNull(message= "Kapacitet resursa je obavezan")
  @Min(value = 1, message= "Kapacitet resursa mora biti najmanje 1")
  @JsonAlias("capacity")
  private Integer kapacitet;

  @Size(max = 300, message= "Opis opreme može imati najviše 300 karaktera")
  @JsonAlias("equipmentDescription")
  private String opisOpreme;

  @NotNull(message= "Cena resursa po satu je obavezna")
  @DecimalMin(value = "0.0", message= "Cena resursa po satu ne može biti negativna")
  @JsonAlias("hourlyPrice")
  private BigDecimal cenaPoSatu;

  @NotEmpty(message= "Izaberite najmanje jedan sport za resurs")
  @JsonAlias("sportIds")
  private List<Long> identifikatoriSportova = new ArrayList<>();

  public Long getId() {
    return id;
  }
  public void setId(Long v) {
    id = v;
  }
  public String getNaziv() {
    return naziv;
  }
  public void setNaziv(String v) {
    naziv = v;
  }
  public String getTip() {
    return tip;
  }
  public void setTip(String v) {
    tip = v;
  }
  public Integer getKapacitet() {
    return kapacitet;
  }
  public void setKapacitet(Integer v) {
    kapacitet = v;
  }
  public String getOpisOpreme() {
    return opisOpreme;
  }
  public void setOpisOpreme(String v) {
    opisOpreme = v;
  }
  public BigDecimal getCenaPoSatu() {
    return cenaPoSatu;
  }
  public void setCenaPoSatu(BigDecimal v) {
    cenaPoSatu = v;
  }
  public List<Long> getIdentifikatoriSportova() {
    return identifikatoriSportova;
  }
  public void setIdentifikatoriSportova(List<Long> v) {
    identifikatoriSportova = v;
  }
}
