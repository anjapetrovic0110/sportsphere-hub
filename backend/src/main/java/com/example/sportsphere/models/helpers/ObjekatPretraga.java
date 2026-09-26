package com.example.sportsphere.models.helpers;

import com.example.sportsphere.db.ObjekatPretragaProjekcija;
import java.math.BigDecimal;

public class ObjekatPretraga {
  private Long id;
  private String naziv;
  private String grad;
  private String adresa;
  private String sportovi;
  private BigDecimal najmanjaCena;
  public ObjekatPretraga(ObjekatPretragaProjekcija p) {
    id = p.getId();
    naziv = p.getNaziv();
    grad = p.getGrad();
    adresa = p.getAdresa();
    sportovi = p.getSportovi();
    najmanjaCena = p.getNajmanjaCena();
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
  public String getSportovi() {
    return sportovi;
  }
  public BigDecimal getNajmanjaCena() {
    return najmanjaCena;
  }
}
