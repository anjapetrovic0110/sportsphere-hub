package com.example.sportsphere.controllers;

import com.example.sportsphere.models.Sport;
import com.example.sportsphere.models.helpers.AdministratorKorisnikIzmena;
import com.example.sportsphere.models.helpers.AdministratorKorisnikPregled;
import com.example.sportsphere.models.helpers.AdministratorTrenerPregled;
import com.example.sportsphere.models.helpers.NoviSportZahtev;
import com.example.sportsphere.models.helpers.ObjekatZahtevPregled;
import com.example.sportsphere.models.helpers.PorukaOdgovor;
import com.example.sportsphere.models.helpers.RegistracijaPregled;
import com.example.sportsphere.services.AdministratorServis;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdministratorKontroler {
  private final AdministratorServis administratorServis;
  public AdministratorKontroler(AdministratorServis administratorServis) {
    this.administratorServis = administratorServis;
  }

  @GetMapping("/registracije")
  public List<RegistracijaPregled> dohvatiZahteve() {
    return administratorServis.dohvatiZahteve();
  }

  @PatchMapping("/registracije/{id}/odobri")
  public PorukaOdgovor odobri(@PathVariable Long id) {
    administratorServis.obradiZahtev(id, true);
    return new PorukaOdgovor("Zahtev je odobren");
  }

  @PatchMapping("/registracije/{id}/odbij")
  public PorukaOdgovor odbij(@PathVariable Long id) {
    administratorServis.obradiZahtev(id, false);
    return new PorukaOdgovor("Zahtev je odbijen");
  }
  @GetMapping("/objekti")
  public List<ObjekatZahtevPregled> objekti() {
    return administratorServis.dohvatiObjekte();
  }
  @PatchMapping("/objekti/{id}/odobri")
  public PorukaOdgovor odobriObjekat(@PathVariable Long id) {
    administratorServis.obradiObjekat(id, true);
    return new PorukaOdgovor("Objekat je odobren");
  }
  @PatchMapping("/objekti/{id}/odbij")
  public PorukaOdgovor odbijObjekat(@PathVariable Long id) {
    administratorServis.obradiObjekat(id, false);
    return new PorukaOdgovor("Objekat je odbijen");
  }

  @GetMapping("/treneri")
  public List<AdministratorTrenerPregled> treneri() {
    return administratorServis.dohvatiTrenere();
  }

  @PatchMapping("/treneri/{id}/deaktiviraj")
  public PorukaOdgovor deaktivirajTrenera(@PathVariable Long id) {
    administratorServis.deaktivirajTrenera(id);
    return new PorukaOdgovor("Trener je deaktiviran");
  }

  @PostMapping("/sportovi")
  public Sport dodajSport(@Valid @RequestBody NoviSportZahtev zahtev) {
    return administratorServis.dodajSport(zahtev);
  }
  @GetMapping("/korisnici")
  public List<AdministratorKorisnikPregled> korisnici() {
    return administratorServis.dohvatiKorisnike();
  }
  @PutMapping("/korisnici/{id}")
  public AdministratorKorisnikPregled izmeniKorisnika(
      @PathVariable Long id, @Valid @RequestBody AdministratorKorisnikIzmena zahtev) {
    return administratorServis.izmeniKorisnika(id, zahtev);
  }
  @DeleteMapping("/korisnici/{id}")
  public PorukaOdgovor obrisiKorisnika(@PathVariable Long id) {
    administratorServis.obrisiKorisnika(id);
    return new PorukaOdgovor("Korisnički nalog je obrisan");
  }
}
