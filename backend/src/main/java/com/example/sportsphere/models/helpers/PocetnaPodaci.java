package com.example.sportsphere.models.helpers;

import java.util.List;

public class PocetnaPodaci {
  private long brojObjekata;
  private List<TopObjekat> topObjekti;
  private List<PromocijaPregled> promocije;
  public PocetnaPodaci(
      long brojObjekata, List<TopObjekat> topObjekti, List<PromocijaPregled> promocije) {
    this.brojObjekata = brojObjekata;
    this.topObjekti = topObjekti;
    this.promocije = promocije;
  }
  public long getBrojObjekata() {
    return brojObjekata;
  }
  public List<TopObjekat> getTopObjekti() {
    return topObjekti;
  }
  public List<PromocijaPregled> getPromocije() {
    return promocije;
  }
}
