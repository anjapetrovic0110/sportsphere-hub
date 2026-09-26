package com.example.sportsphere.models.helpers;
import com.example.sportsphere.models.Korisnik;
import com.example.sportsphere.models.Organizacija;
import java.util.List;
public class ZaposleniProfil {
  private String korisnickoIme, ime, prezime, telefon, email, urlProfilneSlike;
  private String nazivOrganizacije, registrovanaAdresa, maticniBroj, poreskiBroj;
  private List<ZaposleniObjekatPregled> objekti;
  public ZaposleniProfil(
      Korisnik k, Organizacija o, String url, List<ZaposleniObjekatPregled> objekti) {
    korisnickoIme = k.getKorisnickoIme();
    ime = k.getIme();
    prezime = k.getPrezime();
    telefon = k.getTelefon();
    email = k.getEmail();
    urlProfilneSlike = url;
    nazivOrganizacije = o.getNaziv();
    registrovanaAdresa = o.getRegistrovanaAdresa();
    maticniBroj = o.getMaticniBroj();
    poreskiBroj = o.getPoreskiBroj();
    this.objekti = objekti;
  }
  public String getKorisnickoIme() {
    return korisnickoIme;
  }
  public String getIme() {
    return ime;
  }
  public String getPrezime() {
    return prezime;
  }
  public String getTelefon() {
    return telefon;
  }
  public String getEmail() {
    return email;
  }
  public String getUrlProfilneSlike() {
    return urlProfilneSlike;
  }
  public String getNazivOrganizacije() {
    return nazivOrganizacije;
  }
  public String getRegistrovanaAdresa() {
    return registrovanaAdresa;
  }
  public String getMaticniBroj() {
    return maticniBroj;
  }
  public String getPoreskiBroj() {
    return poreskiBroj;
  }
  public List<ZaposleniObjekatPregled> getObjekti() {
    return objekti;
  }
}
