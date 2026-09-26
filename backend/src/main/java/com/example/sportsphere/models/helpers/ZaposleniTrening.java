package com.example.sportsphere.models.helpers;

import com.example.sportsphere.db.ZaposleniTreningProjekcija;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ZaposleniTrening {
  private Long id;
  private String sportista;
  private String trener;
  private String objekat;
  private String sport;
  private LocalDateTime pocetak;
  private LocalDateTime kraj;
  private String status;
  private BigDecimal cena;
  private boolean mozeEvidencija;

  public ZaposleniTrening(ZaposleniTreningProjekcija trening) {
    id = trening.getId();
    sportista = trening.getSportista();
    trener = trening.getTrener();
    objekat = trening.getObjekat();
    sport = trening.getSport();
    pocetak = trening.getPocetak();
    kraj = trening.getKraj();
    status = trening.getStatus();
    cena = trening.getCena();
    mozeEvidencija = trening.getMozeEvidencija() != null && trening.getMozeEvidencija() == 1;
  }

  public Long getId() {
    return id;
  }
  public String getSportista() {
    return sportista;
  }
  public String getTrener() {
    return trener;
  }
  public String getObjekat() {
    return objekat;
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
  public boolean isMozeEvidencija() {
    return mozeEvidencija;
  }
}
