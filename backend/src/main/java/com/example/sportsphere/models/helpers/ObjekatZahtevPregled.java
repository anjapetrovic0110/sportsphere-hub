package com.example.sportsphere.models.helpers;
import com.example.sportsphere.models.Objekat;
public class ObjekatZahtevPregled {
  private Long id;
  private String naziv, grad, adresa, organizacija;
  private int brojResursa;
  public ObjekatZahtevPregled(Objekat o) {
    id = o.getId();
    naziv = o.getNaziv();
    grad = o.getGrad();
    adresa = o.getAdresa();
    organizacija = o.getOrganizacija().getNaziv();
    brojResursa = o.getResursi().size();
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
  public String getOrganizacija() {
    return organizacija;
  }
  public int getBrojResursa() {
    return brojResursa;
  }
}
