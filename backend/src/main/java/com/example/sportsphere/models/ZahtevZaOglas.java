package com.example.sportsphere.models;

public class ZahtevZaOglas {
  private Long id;

  private Oglas oglas;

  private Korisnik sportista;

  private StatusZahteva status = StatusZahteva.PENDING;

  public ZahtevZaOglas() {}

  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public Oglas getOglas() {
    return oglas;
  }
  public void setOglas(Oglas oglas) {
    this.oglas = oglas;
  }
  public Korisnik getSportista() {
    return sportista;
  }
  public void setSportista(Korisnik sportista) {
    this.sportista = sportista;
  }
  public StatusZahteva getStatus() {
    return status;
  }
  public void setStatus(StatusZahteva status) {
    this.status = status;
  }
}
