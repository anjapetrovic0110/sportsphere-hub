package com.example.sportsphere.models.helpers;

import jakarta.validation.constraints.NotBlank;

public class PrijavaZahtev {
  @NotBlank private String korisnickoIme;
  @NotBlank private String lozinka;

  public String getKorisnickoIme() {
    return korisnickoIme;
  }
  public void setKorisnickoIme(String korisnickoIme) {
    this.korisnickoIme = korisnickoIme;
  }
  public String getLozinka() {
    return lozinka;
  }
  public void setLozinka(String lozinka) {
    this.lozinka = lozinka;
  }
}
