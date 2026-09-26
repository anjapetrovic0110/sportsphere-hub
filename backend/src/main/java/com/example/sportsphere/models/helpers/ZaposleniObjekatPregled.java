package com.example.sportsphere.models.helpers;
import com.example.sportsphere.db.ZaposleniObjekatProjekcija;
public class ZaposleniObjekatPregled {
  private Long id;
  private String naziv;
  private String grad;
  private String adresa;
  private String status;
  private String sportovi;
  private String resursi;
  public ZaposleniObjekatPregled(ZaposleniObjekatProjekcija p) {
    id = p.getId();
    naziv = p.getNaziv();
    grad = p.getGrad();
    adresa = p.getAdresa();
    status = p.getStatus();
    sportovi = p.getSportovi();
    resursi = p.getResursi();
  }
  public Long getId() {
    return id;
  }
  public String getNaziv() {
    return naziv;
  }
  public String getGrad() {
    return grad;
  }
  public String getAdresa() {
    return adresa;
  }
  public String getStatus() {
    return status;
  }
  public String getSportovi() {
    return sportovi;
  }
  public String getResursi() {
    return resursi;
  }
}
