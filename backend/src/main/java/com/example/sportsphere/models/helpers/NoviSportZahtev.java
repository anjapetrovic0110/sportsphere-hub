package com.example.sportsphere.models.helpers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NoviSportZahtev {
  @NotBlank(message= "Naziv sporta je obavezan")
  @Size(max = 80, message= "Naziv sporta može imati najviše 80 karaktera")
  private String naziv;

  public String getNaziv() {
    return naziv;
  }
  public void setNaziv(String naziv) {
    this.naziv = naziv;
  }
}
