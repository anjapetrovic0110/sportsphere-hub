package com.example.sportsphere.services;

import com.example.sportsphere.db.KorisnikRepo;
import com.example.sportsphere.db.OpremaRepo;
import com.example.sportsphere.db.OrganizacijaRepo;
import com.example.sportsphere.db.PorudzbinaRepo;
import com.example.sportsphere.db.SportRepo;
import com.example.sportsphere.models.Oprema;
import com.example.sportsphere.models.Porudzbina;
import com.example.sportsphere.models.StatusPorudzbine;
import com.example.sportsphere.models.StavkaPorudzbine;
import com.example.sportsphere.models.Uloga;
import com.example.sportsphere.models.helpers.KorpaZahtev;
import com.example.sportsphere.models.helpers.OpremaPregled;
import com.example.sportsphere.models.helpers.OpremaZahtev;
import com.example.sportsphere.models.helpers.PorudzbinaPregled;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OpremaServis {
  private final OpremaRepo opremaRepozitorijum;
  private final OrganizacijaRepo organizacijaRepozitorijum;
  private final SportRepo sportRepozitorijum;
  private final Path uploadDirektorijum;
  private final KorisnikRepo korisnikRepozitorijum;
  private final PorudzbinaRepo porudzbinaRepozitorijum;

  public OpremaServis(OpremaRepo opremaRepozitorijum, OrganizacijaRepo organizacijaRepozitorijum,
      SportRepo sportRepozitorijum, KorisnikRepo korisnikRepozitorijum,
      PorudzbinaRepo porudzbinaRepozitorijum,
      @Value("${app.upload.directory:uploads}") String direktorijum) {
    this.opremaRepozitorijum = opremaRepozitorijum;
    this.organizacijaRepozitorijum = organizacijaRepozitorijum;
    this.sportRepozitorijum = sportRepozitorijum;
    uploadDirektorijum = Path.of(direktorijum).toAbsolutePath().normalize();
    this.korisnikRepozitorijum = korisnikRepozitorijum;
    this.porudzbinaRepozitorijum = porudzbinaRepozitorijum;
  }

  @Transactional(readOnly = true)
  public List<OpremaPregled> dohvati(String korisnickoIme) {
    Long identifikatorOrganizacije = organizacija(korisnickoIme).getId();
    return opremaRepozitorijum.pronadjiPoOrganizacijiSortiranoPoNazivu(identifikatorOrganizacije)
        .stream()
        .map(OpremaPregled::new)
        .toList();
  }

  @Transactional
  public OpremaPregled dodaj(String korisnickoIme, OpremaZahtev zahtev, MultipartFile slika) {
    var organizacija = organizacija(korisnickoIme);
    String naziv = zahtev.getNaziv().trim();
    if (opremaRepozitorijum.postojiUOrganizacijiSaNazivomBezObziraNaVelicinuSlova(organizacija.getId(), naziv))
      throw new IllegalArgumentException("Oprema sa tim nazivom već postoji");
    Oprema oprema = new Oprema();
    oprema.setOrganizacija(organizacija);
    primeni(oprema, zahtev, slika);
    return new OpremaPregled(opremaRepozitorijum.sacuvaj(oprema));
  }

  @Transactional
  public OpremaPregled izmeni(String korisnickoIme, Long id, OpremaZahtev zahtev, MultipartFile slika) {
    Long identifikatorOrganizacije = organizacija(korisnickoIme).getId();
    Oprema oprema = opremaRepozitorijum.pronadjiPoId(id)
                        .filter(o -> o.getOrganizacija().getId().equals(identifikatorOrganizacije))
                        .orElseThrow(() -> new IllegalArgumentException("Oprema nije pronađena"));
    String naziv = zahtev.getNaziv().trim();
    if (opremaRepozitorijum.postojiUOrganizacijiSaNazivomIDrugacijimIdBezObziraNaVelicinuSlova(identifikatorOrganizacije, naziv, id))
      throw new IllegalArgumentException("Oprema sa tim nazivom već postoji");
    primeni(oprema, zahtev, slika);
    return new OpremaPregled(opremaRepozitorijum.sacuvaj(oprema));
  }

  public Resource dohvatiSliku(String naziv) {
    if (!naziv.matches("[a-f0-9-]+\\.(png|jpg)"))
      throw new IllegalArgumentException("Slika nije pronađena");
    try {
      Path putanja = uploadDirektorijum.resolve("oprema").resolve(naziv).normalize();
      if (!putanja.startsWith(uploadDirektorijum) || !Files.isRegularFile(putanja))
        throw new IllegalArgumentException("Slika nije pronađena");
      return new UrlResource(putanja.toUri());
    } catch (IOException e) {
      throw new IllegalArgumentException("Slika nije pronađena");
    }
  }

  @Transactional(readOnly = true)
  public List<OpremaPregled> dohvatiKatalog(String korisnickoIme, Long identifikatorSporta) {
    sportista(korisnickoIme);
    return opremaRepozitorijum.pronadjiAktivneSortiranoPoNazivu()
        .stream()
        .filter(o -> identifikatorSporta == null || o.getSport().getId().equals(identifikatorSporta))
        .map(OpremaPregled::new)
        .toList();
  }

  @Transactional
  public List<PorudzbinaPregled> naruci(String korisnickoIme, KorpaZahtev zahtev) {
    var kupac = sportista(korisnickoIme);
    Map<Long, Integer> kolicine = new LinkedHashMap<>();
    for (var stavka : zahtev.getStavke())
      kolicine.merge(stavka.getIdentifikatorOpreme(), stavka.getKolicina(), Integer::sum);
    List<Oprema> artikli = opremaRepozitorijum.zakljucajSve(kolicine.keySet().stream().sorted().toList());
    if (artikli.size() != kolicine.size())
      throw new IllegalArgumentException("Neki artikli nisu pronađeni");
    Map<Long, Porudzbina> porudzbine = new LinkedHashMap<>();
    for (Oprema artikal : artikli) {
      int kolicina = kolicine.get(artikal.getId());
      if (!artikal.jeAktivan())
        throw new IllegalArgumentException("Artikal " + artikal.getNaziv() + " nije dostupan");
      if (artikal.getZaliha() < kolicina)
        throw new IllegalArgumentException("Nema dovoljno artikla: " + artikal.getNaziv());
      artikal.setZaliha(artikal.getZaliha() - kolicina);
      opremaRepozitorijum.sacuvaj(artikal);
      Porudzbina porudzbina = porudzbine.computeIfAbsent(artikal.getOrganizacija().getId(), id -> {
        Porudzbina p = new Porudzbina();
        p.setSportista(kupac);
        p.setOrganizacija(artikal.getOrganizacija());
        p.setUkupanIznos(BigDecimal.ZERO);
        return p;
      });
      StavkaPorudzbine stavka = new StavkaPorudzbine();
      stavka.setPorudzbina(porudzbina);
      stavka.setOprema(artikal);
      stavka.setNazivOpreme(artikal.getNaziv());
      stavka.setKolicina(kolicina);
      stavka.setJedinicnaCena(artikal.getCena());
      porudzbina.getStavke().add(stavka);
      porudzbina.setUkupanIznos(porudzbina.getUkupanIznos().add(
          artikal.getCena().multiply(BigDecimal.valueOf(kolicina))));
    }
    return porudzbinaRepozitorijum.sacuvajSve(porudzbine.values())
        .stream()
        .map(PorudzbinaPregled::new)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<PorudzbinaPregled> dohvatiPorudzbine(String korisnickoIme) {
    sportista(korisnickoIme);
    return porudzbinaRepozitorijum.dohvatiZaSportistu(korisnickoIme)
        .stream()
        .map(PorudzbinaPregled::new)
        .toList();
  }

  @Transactional
  public void otkazi(String korisnickoIme, Long id) {
    var kupac = sportista(korisnickoIme);
    Porudzbina p =
        porudzbinaRepozitorijum.zakljucaj(id)
            .filter(x -> x.getSportista().getId().equals(kupac.getId()))
            .orElseThrow(() -> new IllegalArgumentException("Porudžbina nije pronađena"));
    if (p.getStatus() != StatusPorudzbine.ORDERED && p.getStatus() != StatusPorudzbine.ACCEPTED)
      throw new IllegalArgumentException("Porudžbina više ne može biti otkazana");
    vratiZalihe(p);
    p.setStatus(StatusPorudzbine.CANCELLED);
    porudzbinaRepozitorijum.sacuvaj(p);
  }

  @Transactional(readOnly = true)
  public List<PorudzbinaPregled> dohvatiPorudzbineZaposlenog(String korisnickoIme) {
    organizacija(korisnickoIme);
    return porudzbinaRepozitorijum.dohvatiZaZaposlenog(korisnickoIme)
        .stream()
        .map(PorudzbinaPregled::new)
        .toList();
  }

  @Transactional
  public void obradiPorudzbinu(String korisnickoIme, Long id, String akcija) {
    Long identifikatorOrganizacije = organizacija(korisnickoIme).getId();
    Porudzbina p =
        porudzbinaRepozitorijum.zakljucaj(id)
            .filter(x -> x.getOrganizacija().getId().equals(identifikatorOrganizacije))
            .orElseThrow(() -> new IllegalArgumentException("Porudžbina nije pronađena"));
    if (akcija.equals("prihvati") && p.getStatus() == StatusPorudzbine.ORDERED)
      p.setStatus(StatusPorudzbine.ACCEPTED);
    else if (akcija.equals("preuzmi") && p.getStatus() == StatusPorudzbine.ACCEPTED)
      p.setStatus(StatusPorudzbine.PICKED_UP);
    else if (akcija.equals("otkazi")
        && (p.getStatus() == StatusPorudzbine.ORDERED
            || p.getStatus() == StatusPorudzbine.ACCEPTED)) {
      vratiZalihe(p);
      p.setStatus(StatusPorudzbine.CANCELLED);
    } else
      throw new IllegalArgumentException("Promena statusa porudžbine nije dozvoljena");
    porudzbinaRepozitorijum.sacuvaj(p);
  }

  private void vratiZalihe(Porudzbina p) {
    List<Oprema> artikli = opremaRepozitorijum.zakljucajSve(
        p.getStavke().stream().map(i -> i.getOprema().getId()).distinct().sorted().toList());
    Map<Long, Oprema> mapa =
        artikli.stream().collect(java.util.stream.Collectors.toMap(Oprema::getId, o -> o));
    for (var stavka : p.getStavke()) {
      Oprema artikal = mapa.get(stavka.getOprema().getId());
      artikal.setZaliha(artikal.getZaliha() + stavka.getKolicina());
      opremaRepozitorijum.sacuvaj(artikal);
    }
  }

  private void primeni(Oprema oprema, OpremaZahtev zahtev, MultipartFile slika) {
    var sport =
        sportRepozitorijum.pronadjiPoId(zahtev.getIdentifikatorSporta())
            .filter(s -> s.jeAktivan())
            .orElseThrow(() -> new IllegalArgumentException("Izabrani sport nije dostupan"));
    oprema.setNaziv(zahtev.getNaziv().trim());
    oprema.setSport(sport);
    oprema.setCena(zahtev.getCena());
    oprema.setZaliha(zahtev.getZaliha());
    oprema.setAktivan(zahtev.jeAktivan());
    if (slika != null && !slika.isEmpty())
      oprema.setPutanjaSlike(sacuvajSliku(slika));
  }

  private String sacuvajSliku(MultipartFile slika) {
    String tip = slika.getContentType();
    if (!("image/png".equals(tip) || "image/jpeg".equals(tip)))
      throw new IllegalArgumentException("Slika opreme mora biti PNG ili JPG");
    if (slika.getSize() > 5 * 1024 * 1024)
      throw new IllegalArgumentException("Slika može imati najviše 5 MB");
    String naziv = UUID.randomUUID() + ("image/png".equals(tip) ? ".png" : ".jpg");
    try {
      Path direktorijum = uploadDirektorijum.resolve("oprema");
      Files.createDirectories(direktorijum);
      Files.copy(
          slika.getInputStream(), direktorijum.resolve(naziv), StandardCopyOption.REPLACE_EXISTING);
      return naziv;
    } catch (IOException e) {
      throw new IllegalStateException("Slika opreme nije sačuvana");
    }
  }

  private com.example.sportsphere.models.Organizacija organizacija(String korisnickoIme) {
    return organizacijaRepozitorijum.pronadjiPoKorisnickomImenuZaposlenog(korisnickoIme).orElseThrow(
        () -> new IllegalArgumentException("Organizacija nije pronađena"));
  }

  private com.example.sportsphere.models.Korisnik sportista(String korisnickoIme) {
    var korisnik = korisnikRepozitorijum.pronadjiPoKorisnickomImenu(korisnickoIme).orElseThrow(
        () -> new IllegalArgumentException("Korisnik nije pronađen"));
    if (korisnik.getUloga() != Uloga.ATHLETE)
      throw new IllegalArgumentException("Prodavnica je dostupna samo sportistima");
    return korisnik;
  }
}
