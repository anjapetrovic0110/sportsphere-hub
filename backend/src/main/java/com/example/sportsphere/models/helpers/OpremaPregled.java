package com.example.sportsphere.models.helpers;

import com.example.sportsphere.models.Oprema;
import java.math.BigDecimal;

public class OpremaPregled {
  private Long id;
  private String naziv;
  private Long identifikatorSporta;
  private String sport;
  private BigDecimal cena;
  private Integer zaliha;
  private boolean aktivan;
  private String urlSlike;

  public OpremaPregled(Oprema oprema) {
    id = oprema.getId();
    naziv = oprema.getNaziv();
    identifikatorSporta = oprema.getSport().getId();
    sport = oprema.getSport().getNaziv();
    cena = oprema.getCena();
    zaliha = oprema.getZaliha();
    aktivan = oprema.jeAktivan();
    urlSlike = oprema.getPutanjaSlike() == null ? "" : "/javno/slike-opreme/" + oprema.getPutanjaSlike();
  }
  public Long getId() {
    return id;
  }
  public String getNaziv() {
    return naziv;
  }
  public Long getIdentifikatorSporta() {
    return identifikatorSporta;
  }
  public String getSport() {
    return sport;
  }
  public BigDecimal getCena() {
    return cena;
  }
  public Integer getZaliha() {
    return zaliha;
  }
  public boolean isAktivan() {
    return aktivan;
  }
  public String getUrlSlike() {
    return urlSlike;
  }
}
