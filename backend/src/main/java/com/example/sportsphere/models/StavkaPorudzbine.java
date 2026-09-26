package com.example.sportsphere.models;
import java.math.BigDecimal;
public class StavkaPorudzbine {
  private Long id;
  private Porudzbina porudzbina;
  private Oprema oprema;
  private String nazivOpreme;
  private Integer kolicina;
  private BigDecimal jedinicnaCena;
  public StavkaPorudzbine() {}
  public Long getId() {
    return id;
  }
  public void setId(Long v) {
    id = v;
  }
  public Porudzbina getPorudzbina() {
    return porudzbina;
  }
  public void setPorudzbina(Porudzbina v) {
    porudzbina = v;
  }
  public Oprema getOprema() {
    return oprema;
  }
  public void setOprema(Oprema v) {
    oprema = v;
  }
  public String getNazivOpreme() {
    return nazivOpreme;
  }
  public void setNazivOpreme(String v) {
    nazivOpreme = v;
  }
  public Integer getKolicina() {
    return kolicina;
  }
  public void setKolicina(Integer v) {
    kolicina = v;
  }
  public BigDecimal getJedinicnaCena() {
    return jedinicnaCena;
  }
  public void setJedinicnaCena(BigDecimal v) {
    jedinicnaCena = v;
  }
}
