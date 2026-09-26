package com.example.sportsphere.models.helpers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReakcijaZahtev {
  @NotBlank private String reakcija;
  @Size(max = 1000, message= "Komentar može imati najviše 1000 karaktera") private String komentar;
  public String getReakcija() {
    return reakcija;
  }
  public void setReakcija(String v) {
    reakcija = v;
  }
  public String getKomentar() {
    return komentar;
  }
  public void setKomentar(String v) {
    komentar = v;
  }
}
