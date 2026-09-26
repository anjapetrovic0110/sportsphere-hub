package com.example.sportsphere.models;

import java.time.LocalDateTime;

public class IndividualniTrening {
  private Long id;

  private Korisnik sportista;

  private Trener trener;

  private LocalDateTime pocetak;

  private LocalDateTime kraj;

  private StatusTreninga status = StatusTreninga.SCHEDULED;

  public IndividualniTrening() {}

  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public Korisnik getSportista() {
    return sportista;
  }
  public void setSportista(Korisnik sportista) {
    this.sportista = sportista;
  }
  public Trener getTrener() {
    return trener;
  }
  public void setTrener(Trener trener) {
    this.trener = trener;
  }
  public LocalDateTime getPocetak() {
    return pocetak;
  }
  public void setPocetak(LocalDateTime pocetak) {
    this.pocetak = pocetak;
  }
  public LocalDateTime getKraj() {
    return kraj;
  }
  public void setKraj(LocalDateTime kraj) {
    this.kraj = kraj;
  }
  public StatusTreninga getStatus() {
    return status;
  }
  public void setStatus(StatusTreninga status) {
    this.status = status;
  }
}
