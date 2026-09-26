package com.example.sportsphere.services;

import com.example.sportsphere.db.ObjekatRepo;
import com.example.sportsphere.db.PromocijaRepo;
import com.example.sportsphere.models.StatusObjekta;
import com.example.sportsphere.models.helpers.ObjekatDetalji;
import com.example.sportsphere.models.helpers.ObjekatPretraga;
import com.example.sportsphere.models.helpers.PocetnaPodaci;
import com.example.sportsphere.models.helpers.PromocijaPregled;
import com.example.sportsphere.models.helpers.ResursPregled;
import com.example.sportsphere.models.helpers.TopObjekat;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class JavniServis {
  private static final Set<String> TIPOVI_RESURSA =
      Set.of("OUTDOOR_FIELD", "INDOOR_FIELD", "SPORTS_HALL");
  private final ObjekatRepo objekatRepozitorijum;
  private final PromocijaRepo promocijaRepozitorijum;
  private final ReakcijaServis reakcijaServis;

  public JavniServis(ObjekatRepo objekatRepozitorijum, PromocijaRepo promocijaRepozitorijum,
      ReakcijaServis reakcijaServis) {
    this.objekatRepozitorijum = objekatRepozitorijum;
    this.promocijaRepozitorijum = promocijaRepozitorijum;
    this.reakcijaServis = reakcijaServis;
  }

  public PocetnaPodaci dohvatiPocetnu() {
    return new PocetnaPodaci(objekatRepozitorijum.countByStatus(StatusObjekta.ACTIVE),
        objekatRepozitorijum.dohvatiTopTri().stream().map(TopObjekat::new).toList(),
        promocijaRepozitorijum.dohvatiAktuelne(LocalDate.now(), 3)
            .stream()
            .map(PromocijaPregled::new)
            .toList());
  }

  public List<String> dohvatiGradove() {
    return objekatRepozitorijum.dohvatiGradove();
  }

  public List<ObjekatPretraga> pretraziObjekte(
      String naziv, String gradovi, Long identifikatorSporta, String tip, boolean slobodnoDanas) {
    String trazeniNaziv = naziv == null ? "" : naziv.trim();
    String trazeniGradovi = gradovi == null ? "" : gradovi.trim();
    String trazeniTip = tip == null ? "" : tip.trim();
    if (!trazeniTip.isEmpty() && !TIPOVI_RESURSA.contains(trazeniTip)) {
      throw new IllegalArgumentException("Izabrani tip resursa nije ispravan");
    }
    return objekatRepozitorijum.pretrazi(trazeniNaziv, trazeniGradovi, identifikatorSporta, trazeniTip, slobodnoDanas)
        .stream()
        .map(ObjekatPretraga::new)
        .toList();
  }

  public ObjekatDetalji dohvatiDetalje(Long id) {
    var objekat =
        objekatRepozitorijum.pronadjiPoIdIStatusu(id, StatusObjekta.ACTIVE)
            .orElseThrow(() -> new IllegalArgumentException("Sportski objekat nije pronađen"));
    var resursi = objekatRepozitorijum.dohvatiResurse(id).stream().map(ResursPregled::new).toList();
    return new ObjekatDetalji(objekat, objekatRepozitorijum.brojReakcija(id, "LIKE"),
        objekatRepozitorijum.brojReakcija(id, "DISLIKE"), resursi, objekatRepozitorijum.dohvatiSlike(id),
        reakcijaServis.javniKomentari(id));
  }
}
