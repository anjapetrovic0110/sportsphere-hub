package com.example.sportsphere.models.helpers;

import com.example.sportsphere.db.TopObjekatProjekcija;

public class TopObjekat {
  private Long id;
  private String naziv;
  private String grad;
  private Long brojLajkova;
  public TopObjekat(TopObjekatProjekcija p) {
    id = p.getId();
    naziv = p.getNaziv();
    grad = p.getGrad();
    brojLajkova = p.getBrojLajkova();
  }
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getNaziv() {
    return naziv;
  }
  public void setNaziv(String naziv) {
    this.naziv = naziv;
  }
  public String getGrad() {
    return grad;
  }
  public void setGrad(String grad) {
    this.grad = grad;
  }
  public Long getBrojLajkova() {
    return brojLajkova;
  }
  public void setBrojLajkova(Long brojLajkova) {
    this.brojLajkova = brojLajkova;
  }
}
