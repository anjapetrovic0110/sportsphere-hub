package com.example.sportsphere.models.helpers;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.example.sportsphere.models.Porudzbina;
public class PorudzbinaPregled {
  private Long id;
  private String organizacija, sportista, status;
  private BigDecimal ukupno;
  private LocalDateTime kreirano;
  private List<Stavka> stavke;
  public PorudzbinaPregled(Porudzbina p) {
    id = p.getId();
    organizacija = p.getOrganizacija().getNaziv();
    sportista = p.getSportista().getIme() + " " + p.getSportista().getPrezime();
    status = p.getStatus().name();
    ukupno = p.getUkupanIznos();
    kreirano = p.getVremeKreiranja();
    stavke = p.getStavke()
                 .stream()
                 .map(i -> new Stavka(i.getNazivOpreme(), i.getKolicina(), i.getJedinicnaCena()))
                 .toList();
  }
  public Long getId() {
    return id;
  }
  public String getOrganizacija() {
    return organizacija;
  }
  public String getSportista() {
    return sportista;
  }
  public String getStatus() {
    return status;
  }
  public BigDecimal getUkupno() {
    return ukupno;
  }
  public LocalDateTime getKreirano() {
    return kreirano;
  }
  public List<Stavka> getStavke() {
    return stavke;
  }
  public record Stavka(String naziv, Integer kolicina, BigDecimal cena) {}
}
