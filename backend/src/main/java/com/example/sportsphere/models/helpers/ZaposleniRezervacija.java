package com.example.sportsphere.models.helpers;
import com.example.sportsphere.db.ZaposleniRezervacijaProjekcija;
import java.time.LocalDateTime;
public class ZaposleniRezervacija {
  private Long id;
  private String sportista, objekat, resurs, sport, status, tip;
  private LocalDateTime pocetak, kraj;
  private boolean mozeEvidencija;
  public ZaposleniRezervacija(ZaposleniRezervacijaProjekcija p) {
    id = p.getId();
    sportista = p.getSportista();
    objekat = p.getObjekat();
    resurs = p.getResurs();
    sport = p.getSport();
    status = p.getStatus();
    tip = p.getTip();
    pocetak = p.getPocetak();
    kraj = p.getKraj();
    mozeEvidencija = p.getMozeEvidencija() != null && p.getMozeEvidencija() == 1;
  }
  public Long getId() {
    return id;
  }
  public String getSportista() {
    return sportista;
  }
  public String getObjekat() {
    return objekat;
  }
  public String getResurs() {
    return resurs;
  }
  public String getSport() {
    return sport;
  }
  public String getStatus() {
    return status;
  }
  public LocalDateTime getPocetak() {
    return pocetak;
  }
  public LocalDateTime getKraj() {
    return kraj;
  }
  public boolean isMozeEvidencija() {
    return mozeEvidencija;
  }
  public String getTip() {
    return tip;
  }
}
