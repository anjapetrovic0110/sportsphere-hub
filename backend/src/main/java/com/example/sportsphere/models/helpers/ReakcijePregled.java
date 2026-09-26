package com.example.sportsphere.models.helpers;

import java.util.List;

public class ReakcijePregled {
  private long brojLajkova;
  private long brojDislajkova;
  private long preostalo;
  private List<KomentarPregled> komentari;
  public ReakcijePregled(
      long lajkovi, long dislajkovi, long preostalo, List<KomentarPregled> komentari) {
    brojLajkova = lajkovi;
    brojDislajkova = dislajkovi;
    this.preostalo = preostalo;
    this.komentari = komentari;
  }
  public long getBrojLajkova() {
    return brojLajkova;
  }
  public long getBrojDislajkova() {
    return brojDislajkova;
  }
  public long getPreostalo() {
    return preostalo;
  }
  public List<KomentarPregled> getKomentari() {
    return komentari;
  }
}
