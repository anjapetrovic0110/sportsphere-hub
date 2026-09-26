package com.example.sportsphere.models.helpers;
import java.math.BigDecimal;
import java.util.List;
import com.example.sportsphere.db.*;
public class StatistikaPregled {
  private List<SportStavka> sportovi;
  private List<MesecStavka> meseci;
  private List<PotrosnjaStavka> potrosnja;
  public StatistikaPregled(List<SportStatistikaProjekcija> s, List<MesecStatistikaProjekcija> m,
      List<PotrosnjaSportistaProjekcija> p) {
    sportovi = s.stream()
                   .map(x -> new SportStavka(x.getSport(), x.getOdigrano(), x.getRezervisano()))
                   .toList();
    meseci = m.stream().map(x -> new MesecStavka(x.getMesec(), x.getBroj())).toList();
    potrosnja = p.stream().map(x -> new PotrosnjaStavka(x.getSportista(), x.getUkupno())).toList();
  }
  public List<SportStavka> getSportovi() {
    return sportovi;
  }
  public List<MesecStavka> getMeseci() {
    return meseci;
  }
  public List<PotrosnjaStavka> getPotrosnja() {
    return potrosnja;
  }
  public record SportStavka(String sport, Long odigrano, Long rezervisano) {}
  public record MesecStavka(String mesec, Long broj) {}
  public record PotrosnjaStavka(String sportista, BigDecimal ukupno) {}
}
