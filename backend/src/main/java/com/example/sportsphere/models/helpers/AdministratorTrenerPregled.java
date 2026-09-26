package com.example.sportsphere.models.helpers;

import com.example.sportsphere.models.Trener;
import java.math.BigDecimal;

public class AdministratorTrenerPregled {
  private Long id;
  private String ime;
  private String specijalizacija;
  private BigDecimal prosecnaOcena;
  private BigDecimal cenaPoSatu;
  private String objekat;
  private String sport;
  private boolean aktivan;

  public AdministratorTrenerPregled(Trener trener) {
    id = trener.getId();
    ime = trener.getIme() + " " + trener.getPrezime();
    specijalizacija = trener.getSpecijalizacija();
    prosecnaOcena = trener.getProsecnaOcena();
    cenaPoSatu = trener.getCenaPoSatu();
    objekat = trener.getObjekat().getNaziv();
    sport = trener.getSport().getNaziv();
    aktivan = trener.jeAktivan();
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
  public String getObjekat() {
    return objekat;
  }
  public String getSport() {
    return sport;
  }
  public boolean isAktivan() {
    return aktivan;
  }
}
