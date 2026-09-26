package com.example.sportsphere.models.helpers;
import jakarta.validation.constraints.*;
public class AdministratorKorisnikIzmena {
  @NotBlank @Size(max = 80) private String ime;
  @NotBlank @Size(max = 80) private String prezime;
  @NotBlank @Size(max = 30) private String telefon;
  @NotBlank @Email @Size(max = 150) private String email;
  @NotBlank private String status;
  public String getIme() {
    return ime;
  }
  public void setIme(String v) {
    ime = v;
  }
  public String getPrezime() {
    return prezime;
  }
  public void setPrezime(String v) {
    prezime = v;
  }
  public String getTelefon() {
    return telefon;
  }
  public void setTelefon(String v) {
    telefon = v;
  }
  public String getEmail() {
    return email;
  }
  public void setEmail(String v) {
    email = v;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String v) {
    status = v;
  }
}
