package com.example.sportsphere.models.helpers;
import jakarta.validation.constraints.NotBlank;
public class ZahtevZaResetLozinke {
  @NotBlank private String identifikator;
  public String getIdentifikator() {
    return identifikator;
  }
  public void setIdentifikator(String v) {
    identifikator = v;
  }
}
