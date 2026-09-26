package com.example.sportsphere.controllers;

import com.example.sportsphere.db.SportRepo;
import com.example.sportsphere.models.Sport;
import com.example.sportsphere.models.StatusAplikacije;
import com.example.sportsphere.models.helpers.NovaLozinkaZahtev;
import com.example.sportsphere.models.helpers.ObjekatDetalji;
import com.example.sportsphere.models.helpers.ObjekatPretraga;
import com.example.sportsphere.models.helpers.PocetnaPodaci;
import com.example.sportsphere.models.helpers.PorukaOdgovor;
import com.example.sportsphere.models.helpers.RegistracijaZahtev;
import com.example.sportsphere.models.helpers.ResetLozinkeOdgovor;
import com.example.sportsphere.models.helpers.ZahtevZaResetLozinke;
import com.example.sportsphere.services.JavniServis;
import com.example.sportsphere.services.LozinkaServis;
import com.example.sportsphere.services.OpremaServis;
import com.example.sportsphere.services.ProfilServis;
import com.example.sportsphere.services.RegistracijaServis;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/javno")
public class JavniKontroler {
  private final RegistracijaServis registracijaServis;
  private final SportRepo sportRepozitorijum;
  private final JavniServis javniServis;
  private final ProfilServis profilServis;
  private final OpremaServis opremaServis;
  private final LozinkaServis lozinkaServis;

  public JavniKontroler(RegistracijaServis registracijaServis, SportRepo sportRepozitorijum,
      JavniServis javniServis, ProfilServis profilServis, OpremaServis opremaServis,
      LozinkaServis lozinkaServis) {
    this.registracijaServis = registracijaServis;
    this.sportRepozitorijum = sportRepozitorijum;
    this.javniServis = javniServis;
    this.profilServis = profilServis;
    this.opremaServis = opremaServis;
    this.lozinkaServis = lozinkaServis;
  }

  @GetMapping("/status")
  public StatusAplikacije dohvatiStatus() {
    return new StatusAplikacije("SportSphere Hub", "UP", Instant.now());
  }

  @GetMapping("/sportovi")
  public List<Sport> dohvatiSportove() {
    return sportRepozitorijum.pronadjiAktivneSortiranoPoNazivu();
  }

  @GetMapping("/pocetna")
  public PocetnaPodaci dohvatiPocetnu() {
    return javniServis.dohvatiPocetnu();
  }

  @GetMapping("/gradovi")
  public List<String> dohvatiGradove() {
    return javniServis.dohvatiGradove();
  }

  @GetMapping("/objekti")
  public List<ObjekatPretraga> pretraziObjekte(@RequestParam(defaultValue = "") String naziv,
      @RequestParam(defaultValue = "") String gradovi, @RequestParam(required = false) Long identifikatorSporta,
      @RequestParam(defaultValue = "") String tip,
      @RequestParam(defaultValue = "false") boolean slobodnoDanas) {
    return javniServis.pretraziObjekte(naziv, gradovi, identifikatorSporta, tip, slobodnoDanas);
  }

  @GetMapping("/objekti/{id}")
  public ObjekatDetalji dohvatiDetalje(@PathVariable Long id) {
    return javniServis.dohvatiDetalje(id);
  }

  @GetMapping("/profilne-slike/{naziv}")
  public ResponseEntity<Resource> dohvatiProfilnuSliku(@PathVariable String naziv) {
    Resource slika = profilServis.dohvatiSliku(naziv);
    String tip = naziv.endsWith(".png") ? MediaType.IMAGE_PNG_VALUE : MediaType.IMAGE_JPEG_VALUE;
    return ResponseEntity.ok().contentType(MediaType.parseMediaType(tip)).body(slika);
  }

  @GetMapping("/slike-opreme/{naziv}")
  public ResponseEntity<Resource> dohvatiSlikuOpreme(@PathVariable String naziv) {
    Resource slika = opremaServis.dohvatiSliku(naziv);
    String tip = naziv.endsWith(".png") ? MediaType.IMAGE_PNG_VALUE : MediaType.IMAGE_JPEG_VALUE;
    return ResponseEntity.ok().contentType(MediaType.parseMediaType(tip)).body(slika);
  }

  @PostMapping(value = "/registracija", consumes = "multipart/form-data")
  public PorukaOdgovor registracija(@Valid @RequestPart("podaci") RegistracijaZahtev zahtev,
      @RequestPart(value = "slika", required = false) MultipartFile slika) {
    registracijaServis.registruj(zahtev, slika);
    return new PorukaOdgovor("Zahtev za registraciju je poslat administratoru");
  }

  @PostMapping("/lozinka/zahtev")
  public ResetLozinkeOdgovor zahtevZaLozinku(@Valid @RequestBody ZahtevZaResetLozinke zahtev) {
    return lozinkaServis.zahtev(zahtev);
  }

  @PostMapping("/lozinka/reset")
  public PorukaOdgovor novaLozinka(@Valid @RequestBody NovaLozinkaZahtev zahtev) {
    lozinkaServis.promeni(zahtev);
    return new PorukaOdgovor("Lozinka je uspešno promenjena");
  }
}
