package com.example.sportsphere.models.helpers;

import com.example.sportsphere.db.RezervacijaProjekcija;
import java.time.LocalDateTime;

public class RezervacijaPregled {
  private Long id;
  private String objekat;
  private String grad;
  private String resurs;
  private String sport;
  private LocalDateTime pocetak;
  private LocalDateTime kraj;
  private String status;
  private boolean mozeOtkazivanje;

  public RezervacijaPregled(RezervacijaProjekcija rezervacija, LocalDateTime granica) {
    id = rezervacija.getId();
    objekat = rezervacija.getObjekat();
    grad = rezervacija.getGrad();
    resurs = rezervacija.getResurs();
    sport = rezervacija.getSport();
    pocetak = rezervacija.getPocetak();
    kraj = rezervacija.getKraj();
    status = rezervacija.getStatus();
    mozeOtkazivanje =
        (status.equals("PENDING") || status.equals("CONFIRMED")) && !pocetak.isBefore(granica);
  }

  public Long getId() {
    return id;
  }
  public String getObjekat() {
    return objekat;
  }
  public String getGrad() {
    return grad;
  }
  public String getResurs() {
    return resurs;
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
  public boolean isMozeOtkazivanje() {
    return mozeOtkazivanje;
  }
}
