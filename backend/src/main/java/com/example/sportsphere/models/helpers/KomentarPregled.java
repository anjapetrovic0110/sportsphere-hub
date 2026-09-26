package com.example.sportsphere.models.helpers;

import com.example.sportsphere.models.ReakcijaObjekta;
import java.time.LocalDateTime;

public class KomentarPregled {
  private Long id;
  private String autor;
  private String reakcija;
  private String komentar;
  private LocalDateTime vreme;
  private boolean moj;
  public KomentarPregled(ReakcijaObjekta r, String korisnickoIme) {
    id = r.getId();
    autor = r.getSportista().getIme() + " " + r.getSportista().getPrezime();
    reakcija = r.getReakcija().name();
    komentar = r.getKomentar();
    vreme = r.getVremeKreiranja();
    moj = korisnickoIme != null && korisnickoIme.equals(r.getSportista().getKorisnickoIme());
  }
  public Long getId() {
    return id;
  }
  public String getAutor() {
    return autor;
  }
  public String getReakcija() {
    return reakcija;
  }
  public String getKomentar() {
    return komentar;
  }
  public LocalDateTime getVreme() {
    return vreme;
  }
  public boolean isMoj() {
    return moj;
  }
}
