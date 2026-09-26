package com.example.sportsphere.models.helpers;

import com.example.sportsphere.models.IndividualniTrening;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TreningPregled {
  private Long id;
  private String trener;
  private String objekat;
  private String grad;
  private String sport;
  private LocalDateTime pocetak;
  private LocalDateTime kraj;
  private String status;
  private BigDecimal cena;

  public TreningPregled(IndividualniTrening trening) {
    id = trening.getId();
    trener = trening.getTrener().getIme() + " " + trening.getTrener().getPrezime();
    objekat = trening.getTrener().getObjekat().getNaziv();
    grad = trening.getTrener().getObjekat().getGrad();
    sport = trening.getTrener().getSport().getNaziv();
    pocetak = trening.getPocetak();
    kraj = trening.getKraj();
    status = trening.getStatus().name();
    cena = trening.getTrener().getCenaPoSatu();
  }

  public Long getId() {
    return id;
  }
  public String getTrener() {
    return trener;
  }
  public String getObjekat() {
    return objekat;
  }
  public String getGrad() {
    return grad;
  }
  public String getSport() {
    return sport;
  }
  public LocalDateTime getPocetak() {
    return pocetak;
  }
  public LocalDateTime getKraj() {
    return kraj;
  }
  public String getStatus() {
    return status;
  }
  public BigDecimal getCena() {
    return cena;
  }
}
