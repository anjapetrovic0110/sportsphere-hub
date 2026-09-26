package com.example.sportsphere.models.helpers;

import com.example.sportsphere.models.Promocija;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PromocijaPregled {
  private Long id;
  private String naziv;
  private String objekat;
  private LocalDate datumOd;
  private LocalDate datumDo;
  private BigDecimal procenat;
  private BigDecimal iznos;
  public PromocijaPregled(Promocija p) {
    id = p.getId();
    naziv = p.getNaziv();
    objekat = p.getObjekat().getNaziv();
    datumOd = p.getDatumPocetka();
    datumDo = p.getDatumZavrsetka();
    procenat = p.getProcenatPopusta();
    iznos = p.getIznosPopusta();
  }
  public Long getId() {
    return id;
  }
  public String getNaziv() {
    return naziv;
  }
  public String getObjekat() {
    return objekat;
  }
  public LocalDate getDatumOd() {
    return datumOd;
  }
  public LocalDate getDatumDo() {
    return datumDo;
  }
  public BigDecimal getProcenat() {
    return procenat;
  }
  public BigDecimal getIznos() {
    return iznos;
  }
}
