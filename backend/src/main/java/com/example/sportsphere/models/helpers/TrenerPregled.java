package com.example.sportsphere.models.helpers;

import com.example.sportsphere.models.Trener;
import java.math.BigDecimal;
import java.time.LocalTime;

public class TrenerPregled {
  private Long id;
  private String ime;
  private String specijalizacija;
  private BigDecimal prosecnaOcena;
  private BigDecimal cenaPoSatu;
  private Long identifikatorObjekta;
  private String objekat;
  private Long identifikatorSporta;
  private String sport;
  private LocalTime radnoVremeOd;
  private LocalTime radnoVremeDo;

  public TrenerPregled(Trener trener) {
    id = trener.getId();
    ime = trener.getIme() + " " + trener.getPrezime();
    specijalizacija = trener.getSpecijalizacija();
    prosecnaOcena = trener.getProsecnaOcena();
    cenaPoSatu = trener.getCenaPoSatu();
    identifikatorObjekta = trener.getObjekat().getId();
    objekat = trener.getObjekat().getNaziv();
    identifikatorSporta = trener.getSport().getId();
    sport = trener.getSport().getNaziv();
    radnoVremeOd = trener.getObjekat().getVremeOtvaranja();
    radnoVremeDo = trener.getObjekat().getVremeZatvaranja();
  }

  public Long getId() {
    return id;
  }
  public String getIme() {
    return ime;
  }
  public String getSpecijalizacija() {
    return specijalizacija;
  }
  public BigDecimal getProsecnaOcena() {
    return prosecnaOcena;
  }
  public BigDecimal getCenaPoSatu() {
    return cenaPoSatu;
  }
  public Long getIdentifikatorObjekta() {
    return identifikatorObjekta;
  }
  public String getObjekat() {
    return objekat;
  }
  public Long getIdentifikatorSporta() {
    return identifikatorSporta;
  }
  public String getSport() {
    return sport;
  }
  public LocalTime getRadnoVremeOd() {
    return radnoVremeOd;
  }
  public LocalTime getRadnoVremeDo() {
    return radnoVremeDo;
  }
}
