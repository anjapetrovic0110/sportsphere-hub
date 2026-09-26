package com.example.sportsphere.models.helpers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class IzmenaProfilaZahtev {
  @NotBlank @Size(max = 80) private String ime;
  @NotBlank @Size(max = 80) private String prezime;
  @NotBlank @Size(max = 30) private String telefon;
  @NotBlank @Email @Size(max = 150) private String email;
  @Size(max = 5) private List<Long> identifikatoriOmiljenihSportova = new ArrayList<>();

  public String getIme() {
    return ime;
  }
  public void setIme(String ime) {
    this.ime = ime;
  }
  public String getPrezime() {
    return prezime;
  }
  public void setPrezime(String prezime) {
    this.prezime = prezime;
  }
  public String getTelefon() {
    return telefon;
  }
  public void setTelefon(String telefon) {
    this.telefon = telefon;
  }
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }
  public List<Long> getIdentifikatoriOmiljenihSportova() {
    return identifikatoriOmiljenihSportova;
  }
  public void setIdentifikatoriOmiljenihSportova(List<Long> identifikatoriOmiljenihSportova) {
    this.identifikatoriOmiljenihSportova = identifikatoriOmiljenihSportova;
  }
}
