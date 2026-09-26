package com.example.sportsphere.models.helpers;

import com.example.sportsphere.models.Objekat;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public class ObjekatDetalji {
  private Long id;
  private String naziv;
  private String grad;
  private String adresa;
  private String opis;
  private BigDecimal geografskaSirina;
  private BigDecimal geografskaDuzina;
  private LocalTime radnoVremeOd;
  private LocalTime radnoVremeDo;
  private long brojLajkova;
  private long brojDislajkova;
  private List<ResursPregled> resursi;
  private List<String> slike;
  private List<KomentarPregled> komentari;
  public ObjekatDetalji(Objekat o, long lajkovi, long dislajkovi, List<ResursPregled> resursi,
      List<String> slike, List<KomentarPregled> komentari) {
    id = o.getId();
    naziv = o.getNaziv();
    grad = o.getGrad();
    adresa = o.getAdresa();
    opis = o.getOpis();
    geografskaSirina = o.getGeografskaSirina();
    geografskaDuzina = o.getGeografskaDuzina();
    radnoVremeOd = o.getVremeOtvaranja();
    radnoVremeDo = o.getVremeZatvaranja();
    brojLajkova = lajkovi;
    brojDislajkova = dislajkovi;
    this.resursi = resursi;
    this.slike = slike;
    this.komentari = komentari;
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
  public String getOpis() {
    return opis;
  }
  public BigDecimal getGeografskaSirina() {
    return geografskaSirina;
  }
  public BigDecimal getGeografskaDuzina() {
    return geografskaDuzina;
  }
  public LocalTime getRadnoVremeOd() {
    return radnoVremeOd;
  }
  public LocalTime getRadnoVremeDo() {
    return radnoVremeDo;
  }
  public long getBrojLajkova() {
    return brojLajkova;
  }
  public long getBrojDislajkova() {
    return brojDislajkova;
  }
  public List<ResursPregled> getResursi() {
    return resursi;
  }
  public List<String> getSlike() {
    return slike;
  }
  public List<KomentarPregled> getKomentari() {
    return komentari;
  }
}
