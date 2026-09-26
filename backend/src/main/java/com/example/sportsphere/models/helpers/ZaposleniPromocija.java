package com.example.sportsphere.models.helpers;

import com.example.sportsphere.models.Promocija;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ZaposleniPromocija {
  private Long id;
  private Long identifikatorObjekta;
  private String objekat;
  private String naziv;
  private LocalDate datumPocetka;
  private LocalDate datumZavrsetka;
  private BigDecimal procenatPopusta;
  private BigDecimal iznosPopusta;
  private Long identifikatorSporta;
  private String sport;
  private boolean aktivan;

  public ZaposleniPromocija(Promocija promocija) {
    id = promocija.getId();
    identifikatorObjekta = promocija.getObjekat().getId();
    objekat = promocija.getObjekat().getNaziv();
    naziv = promocija.getNaziv();
    datumPocetka = promocija.getDatumPocetka();
    datumZavrsetka = promocija.getDatumZavrsetka();
    procenatPopusta = promocija.getProcenatPopusta();
    iznosPopusta = promocija.getIznosPopusta();
    identifikatorSporta = promocija.getSport() == null ? null : promocija.getSport().getId();
    sport = promocija.getSport() == null ? "Svi sportovi" : promocija.getSport().getNaziv();
    aktivan = promocija.jeAktivan();
  }

  public Long getId() {
    return id;
  }
  public Long getIdentifikatorObjekta() {
    return identifikatorObjekta;
  }
  public String getObjekat() {
    return objekat;
  }
  public String getNaziv() {
    return naziv;
  }
  public LocalDate getDatumPocetka() {
    return datumPocetka;
  }
  public LocalDate getDatumZavrsetka() {
    return datumZavrsetka;
  }
  public BigDecimal getProcenatPopusta() {
    return procenatPopusta;
  }
  public BigDecimal getIznosPopusta() {
    return iznosPopusta;
  }
  public Long getIdentifikatorSporta() {
    return identifikatorSporta;
  }
  public String getSport() {
    return sport;
  }
  public boolean isAktivan() {
    return aktivan;
  }
}
