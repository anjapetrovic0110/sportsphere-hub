package com.example.sportsphere.services;

import com.example.sportsphere.db.IndividualniTreningRepo;
import com.example.sportsphere.db.KorisnikRepo;
import com.example.sportsphere.db.RezervacijaRepo;
import com.example.sportsphere.db.TrenerRepo;
import com.example.sportsphere.models.IndividualniTrening;
import com.example.sportsphere.models.Korisnik;
import com.example.sportsphere.models.StatusObjekta;
import com.example.sportsphere.models.StatusTreninga;
import com.example.sportsphere.models.Trener;
import com.example.sportsphere.models.Uloga;
import com.example.sportsphere.models.helpers.NoviTreningZahtev;
import com.example.sportsphere.models.helpers.TrenerPregled;
import com.example.sportsphere.models.helpers.TreningPregled;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TreningServis {
  private final KorisnikRepo korisnikRepozitorijum;
  private final TrenerRepo trenerRepozitorijum;
  private final IndividualniTreningRepo treningRepozitorijum;
  private final RezervacijaRepo rezervacijaRepozitorijum;

  public TreningServis(KorisnikRepo korisnikRepozitorijum, TrenerRepo trenerRepozitorijum,
      IndividualniTreningRepo treningRepozitorijum, RezervacijaRepo rezervacijaRepozitorijum) {
    this.korisnikRepozitorijum = korisnikRepozitorijum;
    this.trenerRepozitorijum = trenerRepozitorijum;
    this.treningRepozitorijum = treningRepozitorijum;
    this.rezervacijaRepozitorijum = rezervacijaRepozitorijum;
  }

  @Transactional(readOnly = true)
  public List<TrenerPregled> dohvatiTrenere(String korisnickoIme, Long identifikatorObjekta, Long identifikatorSporta) {
    dohvatiSportistu(korisnickoIme);
    return trenerRepozitorijum.pronadjiAktivnePoStatusuObjektaSortiranoPoPrezimenu(StatusObjekta.ACTIVE)
        .stream()
        .filter(t -> identifikatorObjekta == null || t.getObjekat().getId().equals(identifikatorObjekta))
        .filter(t -> identifikatorSporta == null || t.getSport().getId().equals(identifikatorSporta))
        .map(TrenerPregled::new)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<TreningPregled> dohvatiTreninge(String korisnickoIme) {
    dohvatiSportistu(korisnickoIme);
    return treningRepozitorijum.pronadjiPoKorisnickomImenuSportisteSortiranoPoPocetkuOpadajuce(korisnickoIme)
        .stream()
        .map(TreningPregled::new)
        .toList();
  }

  @Transactional
  public void zakazi(String korisnickoIme, NoviTreningZahtev zahtev) {
    Korisnik sportista = dohvatiSportistu(korisnickoIme);
    if (zahtev.getVreme().getMinute() != 0 || zahtev.getVreme().getSecond() != 0)
      throw new IllegalArgumentException("Trening mora početi na pun sat");
    LocalDateTime pocetak = LocalDateTime.of(zahtev.getDatum(), zahtev.getVreme());
    LocalDateTime kraj = pocetak.plusHours(1);
    if (!pocetak.isAfter(LocalDateTime.now()))
      throw new IllegalArgumentException("Termin treninga mora biti u budućnosti");

    Trener trener =
        trenerRepozitorijum.pronadjiPoIdZaAzuriranje(zahtev.getIdentifikatorTrenera())
            .filter(t -> t.jeAktivan() && t.getObjekat().getStatus() == StatusObjekta.ACTIVE)
            .orElseThrow(() -> new IllegalArgumentException("Trener nije dostupan"));
    if (rezervacijaRepozitorijum.blokiranUObjektu(korisnickoIme, trener.getObjekat().getId()) > 0)
      throw new IllegalArgumentException(
          "Nemate pravo rezervacije u ovom objektu zbog nedolazaka");
    if (pocetak.toLocalTime().isBefore(trener.getObjekat().getVremeOtvaranja())
        || kraj.toLocalTime().isAfter(trener.getObjekat().getVremeZatvaranja()))
      throw new IllegalArgumentException("Termin mora biti u okviru radnog vremena objekta");
    if (treningRepozitorijum.postojiPreklapanjeTrenera(
            trener.getId(), StatusTreninga.SCHEDULED, kraj, pocetak))
      throw new IllegalArgumentException("Trener je zauzet u izabranom terminu");
    if (treningRepozitorijum.postojiPreklapanjeSportiste(
            sportista.getId(), StatusTreninga.SCHEDULED, kraj, pocetak))
      throw new IllegalArgumentException("Već imate trening u izabranom terminu");

    IndividualniTrening trening = new IndividualniTrening();
    trening.setSportista(sportista);
    trening.setTrener(trener);
    trening.setPocetak(pocetak);
    trening.setKraj(kraj);
    treningRepozitorijum.sacuvaj(trening);
  }

  private Korisnik dohvatiSportistu(String korisnickoIme) {
    Korisnik korisnik = korisnikRepozitorijum.pronadjiPoKorisnickomImenu(korisnickoIme).orElseThrow(
        () -> new IllegalArgumentException("Korisnik nije pronađen"));
    if (korisnik.getUloga() != Uloga.ATHLETE)
      throw new IllegalArgumentException("Treninzi su dostupni samo sportistima");
    return korisnik;
  }
}
