package com.example.sportsphere.services;

import com.example.sportsphere.db.IndividualniTreningRepo;
import com.example.sportsphere.db.KorisnikRepo;
import com.example.sportsphere.db.ObjekatRepo;
import com.example.sportsphere.db.OrganizacijaRepo;
import com.example.sportsphere.db.RezervacijaRepo;
import com.example.sportsphere.db.SportRepo;
import com.example.sportsphere.models.Korisnik;
import com.example.sportsphere.models.TipResursa;
import com.example.sportsphere.models.Uloga;
import com.example.sportsphere.models.helpers.IzmenaProfilaZahtev;
import com.example.sportsphere.models.helpers.NovaRezervacijaZahtev;
import com.example.sportsphere.models.helpers.PomeranjeRezervacijeZahtev;
import com.example.sportsphere.models.helpers.ProfilPregled;
import com.example.sportsphere.models.helpers.RezervacijaPregled;
import com.example.sportsphere.models.helpers.TerminPregled;
import com.example.sportsphere.models.helpers.ZaposleniObjekatPregled;
import com.example.sportsphere.models.helpers.ZaposleniProfil;
import com.example.sportsphere.models.helpers.ZaposleniRezervacija;
import com.example.sportsphere.models.helpers.ZaposleniTrening;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfilServis {
  private final KorisnikRepo korisnikRepozitorijum;
  private final SportRepo sportRepozitorijum;
  private final Path uploadDirektorijum;
  private final RezervacijaRepo rezervacijaRepozitorijum;
  private final ObjekatRepo objekatRepozitorijum;
  private final OrganizacijaRepo organizacijaRepozitorijum;
  private final IndividualniTreningRepo treningRepozitorijum;

  public ProfilServis(KorisnikRepo korisnikRepozitorijum, SportRepo sportRepozitorijum,
      RezervacijaRepo rezervacijaRepozitorijum, ObjekatRepo objekatRepozitorijum,
      OrganizacijaRepo organizacijaRepozitorijum, IndividualniTreningRepo treningRepozitorijum,
      @Value("${app.upload.directory:uploads}") String uploadDirektorijum) {
    this.korisnikRepozitorijum = korisnikRepozitorijum;
    this.sportRepozitorijum = sportRepozitorijum;
    this.rezervacijaRepozitorijum = rezervacijaRepozitorijum;
    this.objekatRepozitorijum = objekatRepozitorijum;
    this.organizacijaRepozitorijum = organizacijaRepozitorijum;
    this.treningRepozitorijum = treningRepozitorijum;
    this.uploadDirektorijum = Path.of(uploadDirektorijum).toAbsolutePath().normalize();
  }

  @Transactional(readOnly = true)
  public ZaposleniProfil dohvatiProfilZaposlenog(String korisnickoIme) {
    return napraviProfilZaposlenog(dohvatiZaposlenog(korisnickoIme));
  }

  @Transactional
  public ZaposleniProfil izmeniProfilZaposlenog(
      String korisnickoIme, IzmenaProfilaZahtev zahtev, MultipartFile slika) {
    Korisnik korisnik = dohvatiZaposlenog(korisnickoIme);
    String email = zahtev.getEmail().trim().toLowerCase(Locale.ROOT);
    if (korisnikRepozitorijum.postojiSaEmailomIDrugacijimId(email, korisnik.getId()))
      throw new IllegalArgumentException("Imejl adresa je zauzeta");
    korisnik.setIme(zahtev.getIme().trim());
    korisnik.setPrezime(zahtev.getPrezime().trim());
    korisnik.setTelefon(zahtev.getTelefon().trim());
    korisnik.setEmail(email);
    if (slika != null && !slika.isEmpty())
      korisnik.setPutanjaProfilneSlike(sacuvajSliku(slika));
    return napraviProfilZaposlenog(korisnikRepozitorijum.sacuvaj(korisnik));
  }

  @Transactional(readOnly = true)
  public List<RezervacijaPregled> dohvatiRezervacije(String korisnickoIme) {
    dohvatiSportistu(korisnickoIme);
    LocalDateTime granica = LocalDateTime.now().plusHours(12);
    return rezervacijaRepozitorijum.dohvatiZaSportistu(korisnickoIme)
        .stream()
        .map(rezervacija -> new RezervacijaPregled(rezervacija, granica))
        .toList();
  }

  @Transactional
  public void otkaziRezervaciju(String korisnickoIme, Long id) {
    dohvatiSportistu(korisnickoIme);
    int promenjeno = rezervacijaRepozitorijum.otkazi(id, korisnickoIme, LocalDateTime.now().plusHours(12));
    if (promenjeno == 0)
      throw new IllegalArgumentException(
          "Rezervacija ne postoji ili je ostalo manje od 12 sati do početka");
  }

  @Transactional(readOnly = true)
  public List<TerminPregled> dohvatiRaspored(String korisnickoIme, Long identifikatorResursa) {
    dohvatiSportistu(korisnickoIme);
    LocalDateTime od = java.time.LocalDate.now().atStartOfDay();
    return rezervacijaRepozitorijum.dohvatiZauzete(identifikatorResursa, od, od.plusDays(7))
        .stream()
        .map(TerminPregled::new)
        .toList();
  }

  @Transactional
  public void rezervisi(String korisnickoIme, NovaRezervacijaZahtev zahtev) {
    dohvatiSportistu(korisnickoIme);
    var resurs =
        rezervacijaRepozitorijum.zakljucajResurs(zahtev.getIdentifikatorResursa())
            .orElseThrow(() -> new IllegalArgumentException("Teren ili hala nisu dostupni"));
    if (rezervacijaRepozitorijum.blokiran(korisnickoIme, zahtev.getIdentifikatorResursa()) > 0)
      throw new IllegalArgumentException("Nemate pravo rezervacije u ovom objektu zbog nedolazaka");
    if (zahtev.getSatDo() <= zahtev.getSatOd())
      throw new IllegalArgumentException("Rezervacija mora trajati najmanje jedan sat");
    LocalDateTime pocetak = zahtev.getDatum().atTime(zahtev.getSatOd(), 0);
    LocalDateTime kraj = zahtev.getDatum()
                             .plusDays(zahtev.getSatDo() == 24 ? 1 : 0)
                             .atTime(zahtev.getSatDo() == 24 ? 0 : zahtev.getSatDo(), 0);
    if (!pocetak.isAfter(LocalDateTime.now()))
      throw new IllegalArgumentException("Termin mora biti u budućnosti");
    if (pocetak.toLocalTime().isBefore(resurs.getOtvaranje())
        || kraj.isAfter(zahtev.getDatum().atTime(resurs.getZatvaranje())))
      throw new IllegalArgumentException("Termin mora biti u okviru radnog vremena objekta");
    if (rezervacijaRepozitorijum.podrzavaSport(zahtev.getIdentifikatorResursa(), zahtev.getIdentifikatorSporta()) == 0)
      throw new IllegalArgumentException("Izabrani sport nije dostupan na ovom resursu");
    if (rezervacijaRepozitorijum.brojPreklapanja(zahtev.getIdentifikatorResursa(), pocetak, kraj) > 0)
      throw new IllegalArgumentException("Izabrani termin je već zauzet");
    if (rezervacijaRepozitorijum.rezervisi(
            korisnickoIme, zahtev.getIdentifikatorResursa(), zahtev.getIdentifikatorSporta(), pocetak, kraj)
        != 1)
      throw new IllegalArgumentException("Rezervacija nije sačuvana");
  }

  @Transactional(readOnly = true)
  public List<ZaposleniRezervacija> dohvatiRezervacijeZaposlenog(String korisnickoIme) {
    dohvatiZaposlenog(korisnickoIme);
    return rezervacijaRepozitorijum.dohvatiZaZaposlenog(korisnickoIme)
        .stream()
        .map(ZaposleniRezervacija::new)
        .toList();
  }

  @Transactional
  public void evidentirajDolazak(String korisnickoIme, Long id, boolean dosao) {
    dohvatiZaposlenog(korisnickoIme);
    if (rezervacijaRepozitorijum.evidentiraj(korisnickoIme, id, dosao ? "ATTENDED" : "NO_SHOW") != 1)
      throw new IllegalArgumentException(
          "Evidencija je dozvoljena do 10 minuta nakon početka termina");
    if (!dosao) {
      var podaci = rezervacijaRepozitorijum.podaciNedolaska(id).orElseThrow();
      if (treningRepozitorijum.ukupanBrojNedolazaka(podaci.getIdentifikatorSportiste(), podaci.getIdentifikatorObjekta())
          >= podaci.getDozvoljeno())
        rezervacijaRepozitorijum.blokiraj(podaci.getIdentifikatorSportiste(), podaci.getIdentifikatorObjekta());
    }
  }

  @Transactional
  public void pomeriRezervaciju(String korisnickoIme, Long id, PomeranjeRezervacijeZahtev zahtev) {
    dohvatiZaposlenog(korisnickoIme);
    var podaci =
        rezervacijaRepozitorijum.podaciZaPomeranje(korisnickoIme, id)
            .orElseThrow(
                () -> new IllegalArgumentException("Rezervacija nije dostupna za pomeranje"));
    TipResursa tip = TipResursa.valueOf(podaci.getTip());
    if (tip == TipResursa.OUTDOOR_FIELD)
      throw new IllegalArgumentException(
          "Prevlačenjem se mogu pomerati samo termini u zatvorenim terenima i dvoranama");
    rezervacijaRepozitorijum.zakljucajResurs(podaci.getIdentifikatorResursa())
        .orElseThrow(() -> new IllegalArgumentException("Resurs nije dostupan"));
    LocalDateTime pocetak = zahtev.getPocetak();
    if (pocetak.getMinute() != 0 || pocetak.getSecond() != 0
        || !pocetak.isAfter(LocalDateTime.now()))
      throw new IllegalArgumentException("Novi termin mora biti u budućnosti i početi na pun sat");
    LocalDateTime kraj = pocetak.plusMinutes(podaci.getTrajanjeMinuta());
    if (pocetak.toLocalTime().isBefore(podaci.getOtvaranje())
        || kraj.toLocalDate().isAfter(pocetak.toLocalDate())
        || kraj.toLocalTime().isAfter(podaci.getZatvaranje()))
      throw new IllegalArgumentException("Novi termin mora biti u radnom vremenu objekta");
    if (rezervacijaRepozitorijum.pomeri(korisnickoIme, id, pocetak, kraj) != 1)
      throw new IllegalArgumentException("Termin je zauzet ili rezervacija više nije dostupna");
  }

  @Transactional(readOnly = true)
  public List<ZaposleniTrening> dohvatiTreningeZaposlenog(String korisnickoIme) {
    dohvatiZaposlenog(korisnickoIme);
    return treningRepozitorijum.dohvatiZaZaposlenog(korisnickoIme).stream().map(ZaposleniTrening::new).toList();
  }

  @Transactional
  public void evidentirajTrening(String korisnickoIme, Long id, boolean dosao) {
    dohvatiZaposlenog(korisnickoIme);
    if (treningRepozitorijum.evidentiraj(korisnickoIme, id, dosao ? "ATTENDED" : "NO_SHOW") != 1)
      throw new IllegalArgumentException(
          "Evidencija je dozvoljena do 10 minuta nakon početka treninga");
    if (!dosao) {
      var podaci = treningRepozitorijum.podaciNedolaska(id).orElseThrow();
      if (treningRepozitorijum.ukupanBrojNedolazaka(podaci.getIdentifikatorSportiste(), podaci.getIdentifikatorObjekta())
          >= podaci.getDozvoljeno())
        rezervacijaRepozitorijum.blokiraj(podaci.getIdentifikatorSportiste(), podaci.getIdentifikatorObjekta());
    }
  }

  @Transactional(readOnly = true)
  public ProfilPregled dohvati(String korisnickoIme) {
    return napraviPregled(dohvatiSportistu(korisnickoIme));
  }

  @Transactional
  public ProfilPregled izmeni(String korisnickoIme, IzmenaProfilaZahtev zahtev, MultipartFile slika) {
    Korisnik korisnik = dohvatiSportistu(korisnickoIme);
    String email = zahtev.getEmail().trim().toLowerCase(Locale.ROOT);
    if (korisnikRepozitorijum.postojiSaEmailomIDrugacijimId(email, korisnik.getId()))
      throw new IllegalArgumentException("Imejl adresa je zauzeta");
    List<Long> ids = zahtev.getIdentifikatoriOmiljenihSportova() == null
        ? List.of()
        : zahtev.getIdentifikatoriOmiljenihSportova().stream().distinct().toList();
    if (ids.size() > 5)
      throw new IllegalArgumentException("Možete izabrati najviše pet sportova");
    var sportovi = sportRepozitorijum.pronadjiAktivnePoIdentifikatorima(ids);
    if (sportovi.size() != ids.size())
      throw new IllegalArgumentException("Izabran je nepostojeći sport");

    korisnik.setIme(zahtev.getIme().trim());
    korisnik.setPrezime(zahtev.getPrezime().trim());
    korisnik.setTelefon(zahtev.getTelefon().trim());
    korisnik.setEmail(email);
    korisnik.setOmiljeniSportovi(sportovi);
    if (slika != null && !slika.isEmpty())
      korisnik.setPutanjaProfilneSlike(sacuvajSliku(slika));
    return napraviPregled(korisnikRepozitorijum.sacuvaj(korisnik));
  }

  public Resource dohvatiSliku(String naziv) {
    if (!naziv.matches("[a-f0-9-]+\\.(png|jpg)"))
      throw new IllegalArgumentException("Slika nije pronađena");
    try {
      Path putanja = uploadDirektorijum.resolve("profili").resolve(naziv).normalize();
      if (!putanja.startsWith(uploadDirektorijum) || !Files.isRegularFile(putanja))
        throw new IllegalArgumentException("Slika nije pronađena");
      return new UrlResource(putanja.toUri());
    } catch (IOException e) {
      throw new IllegalArgumentException("Slika nije pronađena");
    }
  }

  private Korisnik dohvatiSportistu(String korisnickoIme) {
    Korisnik korisnik = korisnikRepozitorijum.pronadjiPoKorisnickomImenu(korisnickoIme).orElseThrow(
        () -> new IllegalArgumentException("Korisnik nije pronađen"));
    if (korisnik.getUloga() != Uloga.ATHLETE)
      throw new IllegalArgumentException("Profil nije dostupan");
    return korisnik;
  }

  private Korisnik dohvatiZaposlenog(String korisnickoIme) {
    Korisnik korisnik = korisnikRepozitorijum.pronadjiPoKorisnickomImenu(korisnickoIme).orElseThrow(
        () -> new IllegalArgumentException("Korisnik nije pronađen"));
    if (korisnik.getUloga() != Uloga.EMPLOYEE)
      throw new IllegalArgumentException("Profil nije dostupan");
    return korisnik;
  }

  private ZaposleniProfil napraviProfilZaposlenog(Korisnik korisnik) {
    var organizacija =
        organizacijaRepozitorijum.pronadjiPoKorisnickomImenuZaposlenog(korisnik.getKorisnickoIme())
            .orElseThrow(() -> new IllegalArgumentException("Organizacija nije pronađena"));
    String putanja = korisnik.getPutanjaProfilneSlike();
    String url = putanja != null && putanja.startsWith("profili/")
        ? "/javno/profilne-slike/" + putanja.substring("profili/".length())
        : "";
    var objekti = objekatRepozitorijum.dohvatiZaZaposlenog(korisnik.getKorisnickoIme())
                      .stream()
                      .map(ZaposleniObjekatPregled::new)
                      .toList();
    return new ZaposleniProfil(korisnik, organizacija, url, objekti);
  }

  private ProfilPregled napraviPregled(Korisnik korisnik) {
    String putanja = korisnik.getPutanjaProfilneSlike();
    String url = putanja != null && putanja.startsWith("profili/")
        ? "/javno/profilne-slike/" + putanja.substring("profili/".length())
        : "";
    return new ProfilPregled(korisnik, url);
  }

  private String sacuvajSliku(MultipartFile slika) {
    String tip = slika.getContentType();
    if (!("image/jpeg".equals(tip) || "image/png".equals(tip)))
      throw new IllegalArgumentException("Profilna slika mora biti PNG ili JPG");
    if (slika.getSize() > 5 * 1024 * 1024)
      throw new IllegalArgumentException("Profilna slika može imati najviše 5 MB");
    String naziv = UUID.randomUUID() + ("image/png".equals(tip) ? ".png" : ".jpg");
    try {
      Path direktorijum = uploadDirektorijum.resolve("profili");
      Files.createDirectories(direktorijum);
      Files.copy(
          slika.getInputStream(), direktorijum.resolve(naziv), StandardCopyOption.REPLACE_EXISTING);
      return "profili/" + naziv;
    } catch (IOException e) {
      throw new IllegalStateException("Profilna slika nije sačuvana");
    }
  }
}
