package com.example.sportsphere.models.helpers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class RegistracijaZahtev {
  @NotBlank @Size(max = 50) private String korisnickoIme;

  @NotBlank
  @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])[A-Za-z].{7,11}$",
      message=
          "Lozinka mora imati 8-12 znakova, početi slovom i sadržati veliko slovo, broj i specijalan znak")
  private String lozinka;

  @NotBlank @Size(max = 80) private String ime;
  @NotBlank @Size(max = 80) private String prezime;
  @NotBlank @Size(max = 30) private String telefon;
  @NotBlank @Email @Size(max = 150) private String email;
  @NotNull private String uloga;
  @Size(max = 5) private List<Long> identifikatoriOmiljenihSportova = new ArrayList<>();

  private String nazivOrganizacije;
  private String registrovanaAdresa;
  private String maticniBroj;
  private String poreskiBroj;

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
  public String getUloga() {
    return uloga;
  }
  public void setUloga(String uloga) {
    this.uloga = uloga;
  }
  public List<Long> getIdentifikatoriOmiljenihSportova() {
    return identifikatoriOmiljenihSportova;
  }
  public void setIdentifikatoriOmiljenihSportova(List<Long> identifikatoriOmiljenihSportova) {
    this.identifikatoriOmiljenihSportova = identifikatoriOmiljenihSportova;
  }
  public String getNazivOrganizacije() {
    return nazivOrganizacije;
  }
  public void setNazivOrganizacije(String nazivOrganizacije) {
    this.nazivOrganizacije = nazivOrganizacije;
  }
  public String getRegistrovanaAdresa() {
    return registrovanaAdresa;
  }
  public void setRegistrovanaAdresa(String registrovanaAdresa) {
    this.registrovanaAdresa = registrovanaAdresa;
  }
  public String getMaticniBroj() {
    return maticniBroj;
  }
  public void setMaticniBroj(String maticniBroj) {
    this.maticniBroj = maticniBroj;
  }
  public String getPoreskiBroj() {
    return poreskiBroj;
  }
  public void setPoreskiBroj(String poreskiBroj) {
    this.poreskiBroj = poreskiBroj;
  }
}
