package com.example.sportsphere.services;

import com.example.sportsphere.db.KorisnikRepo;
import com.example.sportsphere.db.ObjekatRepo;
import com.example.sportsphere.db.SportRepo;
import com.example.sportsphere.db.TrenerRepo;
import com.example.sportsphere.models.Korisnik;
import com.example.sportsphere.models.Sport;
import com.example.sportsphere.models.StatusKorisnika;
import com.example.sportsphere.models.StatusObjekta;
import com.example.sportsphere.models.Uloga;
import com.example.sportsphere.models.helpers.AdministratorKorisnikIzmena;
import com.example.sportsphere.models.helpers.AdministratorKorisnikPregled;
import com.example.sportsphere.models.helpers.AdministratorTrenerPregled;
import com.example.sportsphere.models.helpers.NoviSportZahtev;
import com.example.sportsphere.models.helpers.ObjekatZahtevPregled;
import com.example.sportsphere.models.helpers.RegistracijaPregled;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministratorServis {
  private final KorisnikRepo korisnikRepozitorijum;
  private final ObjekatRepo objekatRepozitorijum;
  private final TrenerRepo trenerRepozitorijum;
  private final SportRepo sportRepozitorijum;

  public AdministratorServis(KorisnikRepo korisnikRepozitorijum, ObjekatRepo objekatRepozitorijum,
      TrenerRepo trenerRepozitorijum, SportRepo sportRepozitorijum) {
    this.korisnikRepozitorijum = korisnikRepozitorijum;
    this.objekatRepozitorijum = objekatRepozitorijum;
    this.trenerRepozitorijum = trenerRepozitorijum;
    this.sportRepozitorijum = sportRepozitorijum;
  }
  @Transactional(readOnly = true)
  public List<ObjekatZahtevPregled> dohvatiObjekte() {
    return objekatRepozitorijum.pronadjiPoStatusuSortiranoPoNazivu(StatusObjekta.PENDING)
        .stream()
        .map(ObjekatZahtevPregled::new)
        .toList();
  }
  @Transactional
  public void obradiObjekat(Long id, boolean odobren) {
    var o = objekatRepozitorijum.pronadjiPoId(id).orElseThrow(
        () -> new IllegalArgumentException("Objekat ne postoji"));
    if (o.getStatus() != StatusObjekta.PENDING)
      throw new IllegalArgumentException("Objekat je već obrađen");
    o.setStatus(odobren ? StatusObjekta.ACTIVE : StatusObjekta.REJECTED);
    objekatRepozitorijum.sacuvaj(o);
  }

  public List<RegistracijaPregled> dohvatiZahteve() {
    return korisnikRepozitorijum.pronadjiPoStatusuSortiranoPoVremenuKreiranja(StatusKorisnika.PENDING)
        .stream()
        .map(RegistracijaPregled::new)
        .toList();
  }

  @Transactional
  public void obradiZahtev(Long id, boolean odobren) {
    Korisnik korisnik = korisnikRepozitorijum.pronadjiPoId(id).orElseThrow(
        () -> new IllegalArgumentException("Zahtev ne postoji"));
    if (korisnik.getStatus() != StatusKorisnika.PENDING)
      throw new IllegalArgumentException("Zahtev je već obrađen");
    korisnik.setStatus(odobren ? StatusKorisnika.ACTIVE : StatusKorisnika.REJECTED);
    korisnikRepozitorijum.sacuvaj(korisnik);
  }

  @Transactional(readOnly = true)
  public List<AdministratorTrenerPregled> dohvatiTrenere() {
    return trenerRepozitorijum.pronadjiSveSortiranoPoPrezimenuPaImenu()
        .stream()
        .map(AdministratorTrenerPregled::new)
        .toList();
  }

  @Transactional
  public void deaktivirajTrenera(Long id) {
    var trener = trenerRepozitorijum.pronadjiPoIdZaAzuriranje(id).orElseThrow(
        () -> new IllegalArgumentException("Trener ne postoji"));
    if (!trener.jeAktivan())
      throw new IllegalArgumentException("Trener je već deaktiviran");
    trener.setAktivan(false);
    trenerRepozitorijum.sacuvaj(trener);
  }

  @Transactional
  public Sport dodajSport(NoviSportZahtev zahtev) {
    String naziv = zahtev.getNaziv().trim();
    if (sportRepozitorijum.postojiNazivBezObziraNaVelicinuSlova(naziv))
      throw new IllegalArgumentException("Sport sa tim nazivom već postoji");
    Sport sport = new Sport();
    sport.setNaziv(naziv);
    sport.setAktivan(true);
    return sportRepozitorijum.sacuvaj(sport);
  }

  @Transactional(readOnly = true)
  public List<AdministratorKorisnikPregled> dohvatiKorisnike() {
    return korisnikRepozitorijum.pronadjiNeobrisaneSaDrugacijomUlogomSortiranoPoKorisnickomImenu(Uloga.ADMIN)
        .stream()
        .map(AdministratorKorisnikPregled::new)
        .toList();
  }

  @Transactional
  public AdministratorKorisnikPregled izmeniKorisnika(Long id, AdministratorKorisnikIzmena zahtev) {
    Korisnik korisnik =
        korisnikRepozitorijum.pronadjiPoId(id)
            .filter(k -> k.getUloga() != Uloga.ADMIN)
            .orElseThrow(() -> new IllegalArgumentException("Korisnik nije pronađen"));
    String email = zahtev.getEmail().trim().toLowerCase(Locale.ROOT);
    if (korisnikRepozitorijum.postojiSaEmailomIDrugacijimId(email, id))
      throw new IllegalArgumentException("Imejl adresa je zauzeta");
    StatusKorisnika status;
    try {
      status = StatusKorisnika.valueOf(zahtev.getStatus());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Status korisnika nije ispravan");
    }
    korisnik.setIme(zahtev.getIme().trim());
    korisnik.setPrezime(zahtev.getPrezime().trim());
    korisnik.setTelefon(zahtev.getTelefon().trim());
    korisnik.setEmail(email);
    korisnik.setStatus(status);
    return new AdministratorKorisnikPregled(korisnikRepozitorijum.sacuvaj(korisnik));
  }

  @Transactional
  public void obrisiKorisnika(Long id) {
    Korisnik korisnik =
        korisnikRepozitorijum.pronadjiPoId(id)
            .filter(k -> k.getUloga() != Uloga.ADMIN)
            .orElseThrow(() -> new IllegalArgumentException("Korisnik nije pronađen"));
    if (korisnik.jeObrisan())
      throw new IllegalArgumentException("Korisnik je već obrisan");
    String oznaka = "obrisan-" + korisnik.getId();
    korisnik.setKorisnickoIme(oznaka);
    korisnik.setEmail(oznaka + "@sportsphere.invalid");
    korisnik.setIme("Obrisan");
    korisnik.setPrezime("korisnik");
    korisnik.setTelefon("-");
    korisnik.setStatus(StatusKorisnika.DISABLED);
    korisnik.setObrisan(true);
    korisnikRepozitorijum.sacuvaj(korisnik);
  }
}
