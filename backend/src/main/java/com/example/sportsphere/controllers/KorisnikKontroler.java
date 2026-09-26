package com.example.sportsphere.controllers;

import com.example.sportsphere.models.helpers.IzmenaProfilaZahtev;
import com.example.sportsphere.models.helpers.KorpaZahtev;
import com.example.sportsphere.models.helpers.NovaRezervacijaZahtev;
import com.example.sportsphere.models.helpers.NoviObjekatZahtev;
import com.example.sportsphere.models.helpers.NoviOglasZahtev;
import com.example.sportsphere.models.helpers.NoviTreningZahtev;
import com.example.sportsphere.models.helpers.OglasPregled;
import com.example.sportsphere.models.helpers.OpremaPregled;
import com.example.sportsphere.models.helpers.OpremaZahtev;
import com.example.sportsphere.models.helpers.PomeranjeRezervacijeZahtev;
import com.example.sportsphere.models.helpers.PorudzbinaPregled;
import com.example.sportsphere.models.helpers.PorukaOdgovor;
import com.example.sportsphere.models.helpers.ProfilPregled;
import com.example.sportsphere.models.helpers.PromocijaZahtev;
import com.example.sportsphere.models.helpers.ReakcijaZahtev;
import com.example.sportsphere.models.helpers.ReakcijePregled;
import com.example.sportsphere.models.helpers.RezervacijaPregled;
import com.example.sportsphere.models.helpers.StatistikaPregled;
import com.example.sportsphere.models.helpers.TerminPregled;
import com.example.sportsphere.models.helpers.TrenerPregled;
import com.example.sportsphere.models.helpers.TreningPregled;
import com.example.sportsphere.models.helpers.ZaposleniProfil;
import com.example.sportsphere.models.helpers.ZaposleniPromocija;
import com.example.sportsphere.models.helpers.ZaposleniRezervacija;
import com.example.sportsphere.models.helpers.ZaposleniTrening;
import com.example.sportsphere.services.IzvestajServis;
import com.example.sportsphere.services.ObjekatServis;
import com.example.sportsphere.services.OglasServis;
import com.example.sportsphere.services.OpremaServis;
import com.example.sportsphere.services.ProfilServis;
import com.example.sportsphere.services.PromocijaServis;
import com.example.sportsphere.services.ReakcijaServis;
import com.example.sportsphere.services.StatistikaServis;
import com.example.sportsphere.services.TreningServis;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class KorisnikKontroler {
  private final ProfilServis profilServis;
  private final ObjekatServis objekatServis;
  private final OglasServis oglasServis;
  private final TreningServis treningServis;
  private final PromocijaServis promocijaServis;
  private final OpremaServis opremaServis;
  private final IzvestajServis izvestajServis;
  private final ReakcijaServis reakcijaServis;
  private final StatistikaServis statistikaServis;

  public KorisnikKontroler(ProfilServis profilServis, ObjekatServis objekatServis,
      OglasServis oglasServis, TreningServis treningServis, PromocijaServis promocijaServis,
      OpremaServis opremaServis, IzvestajServis izvestajServis, ReakcijaServis reakcijaServis,
      StatistikaServis statistikaServis) {
    this.profilServis = profilServis;
    this.objekatServis = objekatServis;
    this.oglasServis = oglasServis;
    this.treningServis = treningServis;
    this.promocijaServis = promocijaServis;
    this.opremaServis = opremaServis;
    this.izvestajServis = izvestajServis;
    this.reakcijaServis = reakcijaServis;
    this.statistikaServis = statistikaServis;
  }

  @GetMapping("/sportista/status")
  public PorukaOdgovor statusSportiste(Authentication autentikacija) {
    return new PorukaOdgovor("Prijavljen sportista: " + autentikacija.getName());
  }

  @GetMapping("/sportista/profil")
  public ProfilPregled dohvatiProfil(Authentication autentikacija) {
    return profilServis.dohvati(autentikacija.getName());
  }

  @PostMapping(value = "/sportista/profil", consumes = "multipart/form-data")
  public ProfilPregled izmeniProfil(Authentication autentikacija,
      @Valid @RequestPart("podaci") IzmenaProfilaZahtev zahtev,
      @RequestPart(value = "slika", required = false) MultipartFile slika) {
    return profilServis.izmeni(autentikacija.getName(), zahtev, slika);
  }

  @GetMapping("/sportista/rezervacije")
  public List<RezervacijaPregled> dohvatiRezervacije(Authentication autentikacija) {
    return profilServis.dohvatiRezervacije(autentikacija.getName());
  }

  @PostMapping("/sportista/rezervacije/{id}/otkazivanje")
  public PorukaOdgovor otkaziRezervaciju(Authentication autentikacija, @PathVariable Long id) {
    profilServis.otkaziRezervaciju(autentikacija.getName(), id);
    return new PorukaOdgovor("Rezervacija je otkazana");
  }

  @GetMapping("/sportista/resursi/{id}/raspored")
  public List<TerminPregled> dohvatiRaspored(Authentication autentikacija, @PathVariable Long id) {
    return profilServis.dohvatiRaspored(autentikacija.getName(), id);
  }

  @PostMapping("/sportista/rezervacije")
  public PorukaOdgovor rezervisi(
      Authentication autentikacija, @Valid @RequestBody NovaRezervacijaZahtev zahtev) {
    profilServis.rezervisi(autentikacija.getName(), zahtev);
    return new PorukaOdgovor("Zahtev za rezervaciju je poslat");
  }

  @GetMapping("/sportista/objekti/{id}/reakcije")
  public ReakcijePregled reakcije(Authentication autentikacija, @PathVariable Long id) {
    return reakcijaServis.dohvati(autentikacija.getName(), id);
  }

  @PostMapping("/sportista/objekti/{id}/reakcije")
  public ReakcijePregled dodajReakciju(Authentication autentikacija, @PathVariable Long id,
      @Valid @RequestBody ReakcijaZahtev zahtev) {
    return reakcijaServis.dodaj(autentikacija.getName(), id, zahtev);
  }

  @GetMapping("/sportista/statistika")
  public StatistikaPregled statistika(Authentication autentikacija) {
    return statistikaServis.dohvati(autentikacija.getName());
  }

  @GetMapping("/sportista/oglasi")
  public List<OglasPregled> dohvatiOglase(Authentication autentikacija) {
    return oglasServis.dohvatiAktivne(autentikacija.getName());
  }

  @PostMapping("/sportista/oglasi")
  public PorukaOdgovor dodajOglas(
      Authentication autentikacija, @Valid @RequestBody NoviOglasZahtev zahtev) {
    oglasServis.dodaj(autentikacija.getName(), zahtev);
    return new PorukaOdgovor("Oglas je objavljen");
  }

  @PostMapping("/sportista/oglasi/{id}/zahtevi")
  public PorukaOdgovor posaljiZahtev(Authentication autentikacija, @PathVariable Long id) {
    oglasServis.posaljiZahtev(autentikacija.getName(), id);
    return new PorukaOdgovor("Zahtev za pridruživanje je poslat");
  }

  @PostMapping("/sportista/oglasi/{identifikatorOglasa}/zahtevi/{zahtevId}/{akcija}")
  public PorukaOdgovor obradiZahtev(Authentication autentikacija, @PathVariable Long oglasId,
      @PathVariable Long zahtevId, @PathVariable String akcija) {
    if (!akcija.equals("prihvati") && !akcija.equals("odbij"))
      throw new IllegalArgumentException("Akcija nije ispravna");
    oglasServis.obradiZahtev(
        autentikacija.getName(), oglasId, zahtevId, akcija.equals("prihvati"));
    return new PorukaOdgovor(
        akcija.equals("prihvati") ? "Zahtev je prihvaćen" : "Zahtev je odbijen");
  }

  @PostMapping("/sportista/oglasi/{id}/zatvaranje")
  public PorukaOdgovor zatvoriOglas(Authentication autentikacija, @PathVariable Long id) {
    oglasServis.zatvori(autentikacija.getName(), id);
    return new PorukaOdgovor("Oglas je zatvoren");
  }

  @GetMapping("/sportista/treneri")
  public List<TrenerPregled> dohvatiTrenere(Authentication autentikacija,
      @org.springframework.web.bind.annotation.RequestParam(required = false) Long objekatId,
      @org.springframework.web.bind.annotation.RequestParam(required = false) Long identifikatorSporta) {
    return treningServis.dohvatiTrenere(autentikacija.getName(), objekatId, identifikatorSporta);
  }

  @GetMapping("/sportista/treninzi")
  public List<TreningPregled> dohvatiTreninge(Authentication autentikacija) {
    return treningServis.dohvatiTreninge(autentikacija.getName());
  }

  @PostMapping("/sportista/treninzi")
  public PorukaOdgovor zakaziTrening(
      Authentication autentikacija, @Valid @RequestBody NoviTreningZahtev zahtev) {
    treningServis.zakazi(autentikacija.getName(), zahtev);
    return new PorukaOdgovor("Individualni trening je zakazan");
  }

  @GetMapping("/zaposleni/status")
  public PorukaOdgovor statusZaposlenog(Authentication autentikacija) {
    return new PorukaOdgovor("Prijavljen zaposleni: " + autentikacija.getName());
  }

  @GetMapping("/zaposleni/profil")
  public ZaposleniProfil dohvatiProfilZaposlenog(Authentication autentikacija) {
    return profilServis.dohvatiProfilZaposlenog(autentikacija.getName());
  }

  @PostMapping(value = "/zaposleni/profil", consumes = "multipart/form-data")
  public ZaposleniProfil izmeniProfilZaposlenog(Authentication autentikacija,
      @Valid @RequestPart("podaci") IzmenaProfilaZahtev zahtev,
      @RequestPart(value = "slika", required = false) MultipartFile slika) {
    return profilServis.izmeniProfilZaposlenog(autentikacija.getName(), zahtev, slika);
  }

  @PostMapping("/zaposleni/objekti")
  public PorukaOdgovor dodajObjekat(
      Authentication autentikacija, @Valid @RequestBody NoviObjekatZahtev zahtev) {
    objekatServis.dodaj(autentikacija.getName(), zahtev);
    return new PorukaOdgovor("Objekat je poslat administratoru na odobrenje");
  }
  @PostMapping(value = "/zaposleni/objekti/uvoz", consumes = "multipart/form-data")
  public PorukaOdgovor uveziObjekat(
      Authentication autentikacija, @RequestPart("fajl") MultipartFile fajl) {
    objekatServis.uvezi(autentikacija.getName(), fajl);
    return new PorukaOdgovor("Objekat iz JSON fajla je poslat na odobrenje");
  }
  @GetMapping("/zaposleni/objekti/{id}")
  public NoviObjekatZahtev dohvatiObjekat(Authentication autentikacija, @PathVariable Long id) {
    return objekatServis.dohvati(autentikacija.getName(), id);
  }
  @org.springframework.web.bind.annotation.PutMapping("/zaposleni/objekti/{id}")
  public PorukaOdgovor izmeniObjekat(Authentication autentikacija, @PathVariable Long id,
      @Valid @RequestBody NoviObjekatZahtev zahtev) {
    objekatServis.izmeni(autentikacija.getName(), id, zahtev);
    return new PorukaOdgovor("Izmene su poslate administratoru na odobrenje");
  }
  @GetMapping("/zaposleni/rezervacije")
  public List<ZaposleniRezervacija> rezervacijeZaposlenog(Authentication autentikacija) {
    return profilServis.dohvatiRezervacijeZaposlenog(autentikacija.getName());
  }
  @PostMapping("/zaposleni/rezervacije/{id}/{akcija}")
  public PorukaOdgovor evidentiraj(
      Authentication autentikacija, @PathVariable Long id, @PathVariable String akcija) {
    if (!akcija.equals("potvrdi") && !akcija.equals("odjavi"))
      throw new IllegalArgumentException("Akcija nije ispravna");
    profilServis.evidentirajDolazak(autentikacija.getName(), id, akcija.equals("potvrdi"));
    return new PorukaOdgovor(
        akcija.equals("potvrdi") ? "Dolazak je potvrđen" : "Nedolazak je evidentiran");
  }
  @org.springframework.web.bind.annotation.PutMapping("/zaposleni/rezervacije/{id}/termin")
  public PorukaOdgovor pomeriRezervaciju(Authentication autentikacija, @PathVariable Long id,
      @Valid @RequestBody PomeranjeRezervacijeZahtev zahtev) {
    profilServis.pomeriRezervaciju(autentikacija.getName(), id, zahtev);
    return new PorukaOdgovor("Termin je pomeren");
  }
  @GetMapping("/zaposleni/treninzi")
  public List<ZaposleniTrening> treninziZaposlenog(Authentication autentikacija) {
    return profilServis.dohvatiTreningeZaposlenog(autentikacija.getName());
  }

  @PostMapping("/zaposleni/treninzi/{id}/{akcija}")
  public PorukaOdgovor evidentirajTrening(
      Authentication autentikacija, @PathVariable Long id, @PathVariable String akcija) {
    if (!akcija.equals("potvrdi") && !akcija.equals("odjavi"))
      throw new IllegalArgumentException("Akcija nije ispravna");
    profilServis.evidentirajTrening(autentikacija.getName(), id, akcija.equals("potvrdi"));
    return new PorukaOdgovor(akcija.equals("potvrdi") ? "Dolazak na trening je potvrđen"
                                                      : "Nedolazak na trening je evidentiran");
  }

  @GetMapping("/zaposleni/promocije")
  public List<ZaposleniPromocija> promocije(Authentication autentikacija) {
    return promocijaServis.dohvati(autentikacija.getName());
  }

  @PostMapping("/zaposleni/promocije")
  public ZaposleniPromocija dodajPromociju(
      Authentication autentikacija, @Valid @RequestBody PromocijaZahtev zahtev) {
    return promocijaServis.dodaj(autentikacija.getName(), zahtev);
  }

  @org.springframework.web.bind.annotation.PutMapping("/zaposleni/promocije/{id}")
  public ZaposleniPromocija izmeniPromociju(Authentication autentikacija, @PathVariable Long id,
      @Valid @RequestBody PromocijaZahtev zahtev) {
    return promocijaServis.izmeni(autentikacija.getName(), id, zahtev);
  }

  @GetMapping("/zaposleni/oprema")
  public List<OpremaPregled> oprema(Authentication autentikacija) {
    return opremaServis.dohvati(autentikacija.getName());
  }

  @PostMapping(value = "/zaposleni/oprema", consumes = "multipart/form-data")
  public OpremaPregled dodajOpremu(Authentication autentikacija,
      @Valid @RequestPart("podaci") OpremaZahtev zahtev,
      @RequestPart(value = "slika", required = false) MultipartFile slika) {
    return opremaServis.dodaj(autentikacija.getName(), zahtev, slika);
  }

  @org.springframework.web.bind.annotation.
  PutMapping(value = "/zaposleni/oprema/{id}", consumes = "multipart/form-data")
  public OpremaPregled izmeniOpremu(Authentication autentikacija, @PathVariable Long id,
      @Valid @RequestPart("podaci") OpremaZahtev zahtev,
      @RequestPart(value = "slika", required = false) MultipartFile slika) {
    return opremaServis.izmeni(autentikacija.getName(), id, zahtev, slika);
  }

  @GetMapping("/sportista/prodavnica")
  public List<OpremaPregled> katalog(Authentication autentikacija,
      @org.springframework.web.bind.annotation.RequestParam(required = false) Long identifikatorSporta) {
    return opremaServis.dohvatiKatalog(autentikacija.getName(), identifikatorSporta);
  }
  @PostMapping("/sportista/porudzbine")
  public List<PorudzbinaPregled> naruci(
      Authentication autentikacija, @Valid @RequestBody KorpaZahtev zahtev) {
    return opremaServis.naruci(autentikacija.getName(), zahtev);
  }
  @GetMapping("/sportista/porudzbine")
  public List<PorudzbinaPregled> porudzbine(Authentication autentikacija) {
    return opremaServis.dohvatiPorudzbine(autentikacija.getName());
  }
  @PostMapping("/sportista/porudzbine/{id}/otkazivanje")
  public PorukaOdgovor otkaziPorudzbinu(Authentication autentikacija, @PathVariable Long id) {
    opremaServis.otkazi(autentikacija.getName(), id);
    return new PorukaOdgovor("Porudžbina je otkazana");
  }
  @GetMapping("/zaposleni/porudzbine")
  public List<PorudzbinaPregled> porudzbineZaposlenog(Authentication autentikacija) {
    return opremaServis.dohvatiPorudzbineZaposlenog(autentikacija.getName());
  }
  @PostMapping("/zaposleni/porudzbine/{id}/{akcija}")
  public PorukaOdgovor obradiPorudzbinu(
      Authentication autentikacija, @PathVariable Long id, @PathVariable String akcija) {
    if (!java.util.Set.of("prihvati", "preuzmi", "otkazi").contains(akcija))
      throw new IllegalArgumentException("Akcija nije ispravna");
    opremaServis.obradiPorudzbinu(autentikacija.getName(), id, akcija);
    return new PorukaOdgovor("Status porudžbine je promenjen");
  }
  @GetMapping("/zaposleni/izvestaji/{tip}")
  public ResponseEntity<byte[]> izvestaj(Authentication a, @PathVariable String tip,
      @org.springframework.web.bind.annotation.RequestParam String mesec) {
    java.time.YearMonth m;
    try {
      m = java.time.YearMonth.parse(mesec);
    } catch (Exception e) {
      throw new IllegalArgumentException("Mesec nije ispravan");
    }
    byte[] pdf;
    if (tip.equals("popunjenost"))
      pdf = izvestajServis.popunjenost(a.getName(), m);
    else if (tip.equals("promet"))
      pdf = izvestajServis.promet(a.getName(), m);
    else
      throw new IllegalArgumentException("Tip izveštaja nije ispravan");
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=izvestaj-" + tip + "-" + mesec + ".pdf")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf);
  }
}
