package com.example.sportsphere.services;

import com.example.sportsphere.db.KorisnikRepo;
import com.example.sportsphere.db.OglasRepo;
import com.example.sportsphere.db.SportRepo;
import com.example.sportsphere.db.ZahtevZaOglasRepo;
import com.example.sportsphere.models.Korisnik;
import com.example.sportsphere.models.Oglas;
import com.example.sportsphere.models.StatusZahteva;
import com.example.sportsphere.models.Uloga;
import com.example.sportsphere.models.ZahtevZaOglas;
import com.example.sportsphere.models.helpers.NoviOglasZahtev;
import com.example.sportsphere.models.helpers.OglasPregled;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OglasServis {
  private final KorisnikRepo korisnikRepozitorijum;
  private final SportRepo sportRepozitorijum;
  private final OglasRepo oglasRepozitorijum;
  private final ZahtevZaOglasRepo zahtevRepozitorijum;

  public OglasServis(KorisnikRepo korisnikRepozitorijum, SportRepo sportRepozitorijum,
      OglasRepo oglasRepozitorijum, ZahtevZaOglasRepo zahtevRepozitorijum) {
    this.korisnikRepozitorijum = korisnikRepozitorijum;
    this.sportRepozitorijum = sportRepozitorijum;
    this.oglasRepozitorijum = oglasRepozitorijum;
    this.zahtevRepozitorijum = zahtevRepozitorijum;
  }

  @Transactional(readOnly = true)
  public List<OglasPregled> dohvatiAktivne(String korisnickoIme) {
    Korisnik sportista = dohvatiSportistu(korisnickoIme);
    LocalDateTime sada = LocalDateTime.now();
    return oglasRepozitorijum.pronadjiAktivneSortiranoPoDatumuIVremenuIgre()
        .stream()
        .filter(oglas -> LocalDateTime.of(oglas.getDatumIgre(), oglas.getVremeIgre()).isAfter(sada))
        .map(oglas -> napraviPregled(oglas, sportista))
        .toList();
  }

  @Transactional
  public void dodaj(String korisnickoIme, NoviOglasZahtev zahtev) {
    Korisnik sportista = dohvatiSportistu(korisnickoIme);
    LocalDateTime termin = LocalDateTime.of(zahtev.getDatumIgre(), zahtev.getVremeIgre());
    if (!termin.isAfter(LocalDateTime.now()))
      throw new IllegalArgumentException("Termin oglasa mora biti u budućnosti");
    var sport =
        sportRepozitorijum.pronadjiPoId(zahtev.getIdentifikatorSporta())
            .filter(s -> s.jeAktivan())
            .orElseThrow(() -> new IllegalArgumentException("Izabrani sport nije dostupan"));

    Oglas oglas = new Oglas();
    oglas.setVlasnik(sportista);
    oglas.setSport(sport);
    oglas.setGrad(zahtev.getGrad().trim());
    oglas.setDatumIgre(zahtev.getDatumIgre());
    oglas.setVremeIgre(zahtev.getVremeIgre());
    oglas.setNedostajuciIgraci(zahtev.getNedostajuciIgraci());
    oglasRepozitorijum.sacuvaj(oglas);
  }

  @Transactional
  public void posaljiZahtev(String korisnickoIme, Long identifikatorOglasa) {
    Korisnik sportista = dohvatiSportistu(korisnickoIme);
    Oglas oglas = zakljucajAktivanOglas(identifikatorOglasa);
    if (oglas.getVlasnik().getId().equals(sportista.getId()))
      throw new IllegalArgumentException("Ne možete se prijaviti na sopstveni oglas");
    if (zahtevRepozitorijum.postojiZaOglasISportistu(identifikatorOglasa, sportista.getId()))
      throw new IllegalArgumentException("Već ste poslali zahtev za ovaj oglas");

    ZahtevZaOglas zahtev = new ZahtevZaOglas();
    zahtev.setOglas(oglas);
    zahtev.setSportista(sportista);
    zahtevRepozitorijum.sacuvaj(zahtev);
  }

  @Transactional
  public void obradiZahtev(String korisnickoIme, Long identifikatorOglasa, Long zahtevId, boolean prihvati) {
    Korisnik vlasnik = dohvatiSportistu(korisnickoIme);
    Oglas oglas = zakljucajAktivanOglas(identifikatorOglasa);
    proveriVlasnika(oglas, vlasnik);
    ZahtevZaOglas zahtev =
        zahtevRepozitorijum.pronadjiPoId(zahtevId)
            .filter(z -> z.getOglas().getId().equals(identifikatorOglasa))
            .orElseThrow(() -> new IllegalArgumentException("Zahtev nije pronađen"));
    if (zahtev.getStatus() != StatusZahteva.PENDING)
      throw new IllegalArgumentException("Zahtev je već obrađen");

    if (prihvati) {
      long prihvaceno = zahtevRepozitorijum.countByAdIdAndStatus(identifikatorOglasa, StatusZahteva.ACCEPTED);
      if (prihvaceno >= oglas.getNedostajuciIgraci())
        throw new IllegalArgumentException("Ekipa je već kompletirana");
      zahtev.setStatus(StatusZahteva.ACCEPTED);
      if (prihvaceno + 1 >= oglas.getNedostajuciIgraci()) {
        oglas.setAktivan(false);
        oglasRepozitorijum.sacuvaj(oglas);
      }
    } else {
      zahtev.setStatus(StatusZahteva.REJECTED);
    }
    zahtevRepozitorijum.sacuvaj(zahtev);
  }

  @Transactional
  public void zatvori(String korisnickoIme, Long identifikatorOglasa) {
    Korisnik vlasnik = dohvatiSportistu(korisnickoIme);
    Oglas oglas = zakljucajAktivanOglas(identifikatorOglasa);
    proveriVlasnika(oglas, vlasnik);
    oglas.setAktivan(false);
    oglasRepozitorijum.sacuvaj(oglas);
  }

  private OglasPregled napraviPregled(Oglas oglas, Korisnik sportista) {
    List<ZahtevZaOglas> zahtevi = zahtevRepozitorijum.pronadjiPoOglasuSortiranoPoId(oglas.getId());
    boolean moj = oglas.getVlasnik().getId().equals(sportista.getId());
    int prihvaceno =
        (int) zahtevi.stream().filter(z -> z.getStatus() == StatusZahteva.ACCEPTED).count();
    boolean poslat =
        zahtevi.stream().anyMatch(z -> z.getSportista().getId().equals(sportista.getId()));
    List<OglasPregled.ZahtevPregled> pregledi = moj
        ? zahtevi.stream()
              .map(z
                  -> new OglasPregled.ZahtevPregled(z.getId(),
                      z.getSportista().getIme() + " " + z.getSportista().getPrezime(),
                      z.getStatus().name()))
              .toList()
        : List.of();
    return new OglasPregled(oglas.getId(),
        oglas.getVlasnik().getIme() + " " + oglas.getVlasnik().getPrezime(),
        oglas.getSport().getNaziv(), oglas.getGrad(), oglas.getDatumIgre(), oglas.getVremeIgre(),
        oglas.getNedostajuciIgraci(), prihvaceno, moj, poslat, pregledi);
  }

  private Oglas zakljucajAktivanOglas(Long id) {
    Oglas oglas = oglasRepozitorijum.pronadjiPoIdZaAzuriranje(id).orElseThrow(
        () -> new IllegalArgumentException("Oglas nije pronađen"));
    if (!oglas.jeAktivan()
        || !LocalDateTime.of(oglas.getDatumIgre(), oglas.getVremeIgre()).isAfter(LocalDateTime.now()))
      throw new IllegalArgumentException("Oglas više nije aktivan");
    return oglas;
  }

  private void proveriVlasnika(Oglas oglas, Korisnik korisnik) {
    if (!oglas.getVlasnik().getId().equals(korisnik.getId()))
      throw new IllegalArgumentException("Samo autor može upravljati oglasom");
  }

  private Korisnik dohvatiSportistu(String korisnickoIme) {
    Korisnik korisnik = korisnikRepozitorijum.pronadjiPoKorisnickomImenu(korisnickoIme).orElseThrow(
        () -> new IllegalArgumentException("Korisnik nije pronađen"));
    if (korisnik.getUloga() != Uloga.ATHLETE)
      throw new IllegalArgumentException("Oglasi su dostupni samo sportistima");
    return korisnik;
  }
}
