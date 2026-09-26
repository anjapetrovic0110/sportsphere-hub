package com.example.sportsphere.models;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
public class Porudzbina {
  private Long id;
  private Korisnik sportista;
  private Organizacija organizacija;
  private StatusPorudzbine status = StatusPorudzbine.ORDERED;
  private BigDecimal ukupanIznos;
  private LocalDateTime vremeKreiranja;
  private List<StavkaPorudzbine> stavke = new ArrayList<>();
  public Porudzbina() {}
  public Long getId() {
    return id;
  }
  public void setId(Long v) {
    id = v;
  }
  public Korisnik getSportista() {
    return sportista;
  }
  public void setSportista(Korisnik v) {
    sportista = v;
  }
  public Organizacija getOrganizacija() {
    return organizacija;
  }
  public void setOrganizacija(Organizacija v) {
    organizacija = v;
  }
  public StatusPorudzbine getStatus() {
    return status;
  }
  public void setStatus(StatusPorudzbine v) {
    status = v;
  }
  public BigDecimal getUkupanIznos() {
    return ukupanIznos;
  }
  public void setUkupanIznos(BigDecimal v) {
    ukupanIznos = v;
  }
  public LocalDateTime getVremeKreiranja() {
    return vremeKreiranja;
  }
  public void setVremeKreiranja(LocalDateTime v) {
    vremeKreiranja = v;
  }
  public List<StavkaPorudzbine> getStavke() {
    return stavke;
  }
  public void setStavke(List<StavkaPorudzbine> v) {
    stavke = v;
  }
}
