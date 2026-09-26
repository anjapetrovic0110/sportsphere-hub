package com.example.sportsphere.models.helpers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class OglasPregled {
  private Long id;
  private String autor;
  private String sport;
  private String grad;
  private LocalDate datum;
  private LocalTime vreme;
  private int nedostaje;
  private int prihvaceno;
  private boolean moj;
  private boolean poslatZahtev;
  private List<ZahtevPregled> zahtevi;

  public OglasPregled(Long id, String autor, String sport, String grad, LocalDate datum,
      LocalTime vreme, int nedostaje, int prihvaceno, boolean moj, boolean poslatZahtev,
      List<ZahtevPregled> zahtevi) {
    this.id = id;
    this.autor = autor;
    this.sport = sport;
    this.grad = grad;
    this.datum = datum;
    this.vreme = vreme;
    this.nedostaje = nedostaje;
    this.prihvaceno = prihvaceno;
    this.moj = moj;
    this.poslatZahtev = poslatZahtev;
    this.zahtevi = zahtevi;
  }

  public Long getId() {
    return id;
  }
  public String getAutor() {
    return autor;
  }
  public String getSport() {
    return sport;
  }
  public String getGrad() {
    return grad;
  }
  public LocalDate getDatum() {
    return datum;
  }
  public LocalTime getVreme() {
    return vreme;
  }
  public int getNedostaje() {
    return nedostaje;
  }
  public int getPrihvaceno() {
    return prihvaceno;
  }
  public boolean isMoj() {
    return moj;
  }
  public boolean isPoslatZahtev() {
    return poslatZahtev;
  }
  public List<ZahtevPregled> getZahtevi() {
    return zahtevi;
  }

  public static class ZahtevPregled {
    private Long id;
    private String sportista;
    private String status;

    public ZahtevPregled(Long id, String sportista, String status) {
      this.id = id;
      this.sportista = sportista;
      this.status = status;
    }

    public Long getId() {
      return id;
    }
    public String getSportista() {
      return sportista;
    }
    public String getStatus() {
      return status;
    }
  }
}
