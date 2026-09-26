package com.example.sportsphere.services;

import com.example.sportsphere.db.KorisnikRepo;
import com.example.sportsphere.db.ObjekatRepo;
import com.example.sportsphere.db.ReakcijaObjektaRepo;
import com.example.sportsphere.models.ReakcijaObjekta;
import com.example.sportsphere.models.StatusObjekta;
import com.example.sportsphere.models.TipReakcije;
import com.example.sportsphere.models.helpers.KomentarPregled;
import com.example.sportsphere.models.helpers.ReakcijaZahtev;
import com.example.sportsphere.models.helpers.ReakcijePregled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReakcijaServis {
  private final ReakcijaObjektaRepo reakcijaRepozitorijum;
  private final ObjekatRepo objekatRepozitorijum;
  private final KorisnikRepo korisnikRepozitorijum;
  public ReakcijaServis(ReakcijaObjektaRepo reakcijaRepozitorijum,
      ObjekatRepo objekatRepozitorijum, KorisnikRepo korisnikRepozitorijum) {
    this.reakcijaRepozitorijum = reakcijaRepozitorijum;
    this.objekatRepozitorijum = objekatRepozitorijum;
    this.korisnikRepozitorijum = korisnikRepozitorijum;
  }

  @Transactional(readOnly = true)
  public ReakcijePregled dohvati(String korisnickoIme, Long identifikatorObjekta) {
    var sportista = korisnikRepozitorijum.pronadjiPoKorisnickomImenu(korisnickoIme).orElseThrow(
        () -> new IllegalArgumentException("Korisnik nije pronađen"));
    objekatRepozitorijum.pronadjiPoIdIStatusu(identifikatorObjekta, StatusObjekta.ACTIVE)
        .orElseThrow(() -> new IllegalArgumentException("Objekat nije pronađen"));
    long dozvoljeno = reakcijaRepozitorijum.brojPotvrdjenihRezervacija(sportista.getId(), identifikatorObjekta);
    long iskorisceno = reakcijaRepozitorijum.countByFacilityIdAndAthleteId(identifikatorObjekta, sportista.getId());
    return pregled(identifikatorObjekta, korisnickoIme, Math.max(0, dozvoljeno - iskorisceno));
  }

  @Transactional
  public ReakcijePregled dodaj(String korisnickoIme, Long identifikatorObjekta, ReakcijaZahtev zahtev) {
    var sportista = korisnikRepozitorijum.pronadjiPoKorisnickomImenu(korisnickoIme).orElseThrow(
        () -> new IllegalArgumentException("Korisnik nije pronađen"));
    var objekat = objekatRepozitorijum.zakljucajAktivan(identifikatorObjekta, StatusObjekta.ACTIVE)
                      .orElseThrow(() -> new IllegalArgumentException("Objekat nije pronađen"));
    TipReakcije tip;
    try {
      tip = TipReakcije.valueOf(zahtev.getReakcija());
    } catch (Exception e) {
      throw new IllegalArgumentException("Reakcija nije ispravna");
    }
    long dozvoljeno = reakcijaRepozitorijum.brojPotvrdjenihRezervacija(sportista.getId(), identifikatorObjekta);
    long iskorisceno = reakcijaRepozitorijum.countByFacilityIdAndAthleteId(identifikatorObjekta, sportista.getId());
    if (iskorisceno >= dozvoljeno)
      throw new IllegalArgumentException(
          "Broj reakcija ne može biti veći od broja potvrđenih rezervacija");
    ReakcijaObjekta reakcija = new ReakcijaObjekta();
    reakcija.setObjekat(objekat);
    reakcija.setSportista(sportista);
    reakcija.setReakcija(tip);
    String komentar = zahtev.getKomentar() == null ? null : zahtev.getKomentar().trim();
    reakcija.setKomentar(komentar == null || komentar.isEmpty() ? null : komentar);
    reakcijaRepozitorijum.sacuvaj(reakcija);
    return pregled(identifikatorObjekta, korisnickoIme, dozvoljeno - iskorisceno - 1);
  }

  @Transactional(readOnly = true)
  public java.util.List<KomentarPregled> javniKomentari(Long identifikatorObjekta) {
    return reakcijaRepozitorijum.dohvatiKomentare(identifikatorObjekta, 5)
        .stream()
        .map(r -> new KomentarPregled(r, null))
        .toList();
  }
  private ReakcijePregled pregled(Long id, String korisnickoIme, long preostalo) {
    return new ReakcijePregled(reakcijaRepozitorijum.countByFacilityIdAndReaction(id, TipReakcije.LIKE),
        reakcijaRepozitorijum.countByFacilityIdAndReaction(id, TipReakcije.DISLIKE), preostalo,
        reakcijaRepozitorijum.dohvatiKomentare(id, 5)
            .stream()
            .map(r -> new KomentarPregled(r, korisnickoIme))
            .toList());
  }
}
