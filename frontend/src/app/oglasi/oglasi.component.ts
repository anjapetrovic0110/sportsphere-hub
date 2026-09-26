import { DatePipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NoviOglas, Oglas, ZahtevZaOglas } from '../models/oglas';
import { Sport } from '../models/sport';
import { JavniServis } from '../services/javni.service';
import { KorisnikServis } from '../services/korisnik.service';
import { RegistracijaServis } from '../services/registracija.service';

@Component({
  selector: 'app-oglasi',
  standalone: true,
  imports: [FormsModule, DatePipe, RouterLink],
  templateUrl: './oglasi.component.html',
  styleUrl: './oglasi.component.css'
})
export class OglasiKomponenta implements OnInit {
  private korisnikServis = inject(KorisnikServis);
  private registracijaServis = inject(RegistracijaServis);
  private javniServis = inject(JavniServis);
  oglas = new NoviOglas();
  oglasi: Oglas[] = [];
  sportovi: Sport[] = [];
  gradovi: string[] = ['Beograd', 'Novi Sad', 'Niš', 'Kragujevac'];
  vremena = Array.from({ length: 16 }, (_, indeks) => `${String(indeks + 7).padStart(2, '0')}:00`);
  poruka = '';
  uspesno = false;
  ucitavanje = true;
  cuvanje = false;
  danas = new Date().toISOString().slice(0, 10);
  ngOnInit() {
    this.registracijaServis
      .dohvatiSportove()
      .subscribe({ next: podaci => (this.sportovi = podaci) });
    this.javniServis.dohvatiGradove().subscribe({
      next: podaci => {
        if (podaci.length) this.gradovi = podaci;
      }
    });
    this.ucitaj();
  }

  objavi(forma: NgForm) {
    this.poruka = '';
    this.uspesno = false;
    if (forma.invalid) {
      forma.control.markAllAsTouched();
      this.poruka = 'Proverite podatke oglasa';
      return;
    }
    this.cuvanje = true;
    this.korisnikServis.dodajOglas(this.oglas).subscribe({
      next: odgovor => {
        this.poruka = odgovor.poruka;
        this.uspesno = true;
        this.cuvanje = false;
        this.oglas = new NoviOglas();
        forma.resetForm(this.oglas);
        this.ucitaj();
      },
      error: greska => {
        this.greska(greska, 'Oglas nije objavljen');
        this.cuvanje = false;
      }
    });
  }

  pridruziSe(oglas: Oglas) {
    this.korisnikServis.posaljiZahtevZaOglas(oglas.id).subscribe({
      next: odgovor => {
        this.uspeh(odgovor.poruka);
        this.ucitaj();
      },
      error: greska => this.greska(greska, 'Zahtev nije poslat')
    });
  }

  obradi(oglas: Oglas, zahtev: ZahtevZaOglas, akcija: 'prihvati' | 'odbij') {
    this.korisnikServis.obradiZahtevZaOglas(oglas.id, zahtev.id, akcija).subscribe({
      next: odgovor => {
        this.uspeh(odgovor.poruka);
        this.ucitaj();
      },
      error: greska => this.greska(greska, 'Zahtev nije obrađen')
    });
  }

  zatvori(oglas: Oglas) {
    this.korisnikServis.zatvoriOglas(oglas.id).subscribe({
      next: odgovor => {
        this.uspeh(odgovor.poruka);
        this.ucitaj();
      },
      error: greska => this.greska(greska, 'Oglas nije zatvoren')
    });
  }

  nazivStatusa(status: string) {
    return (
      (
        {
          PENDING: 'Na čekanju',
          ACCEPTED: 'Prihvaćen',
          REJECTED: 'Odbijen'
        } as Record<string, string>
      )[status] || status
    );
  }

  private ucitaj() {
    this.korisnikServis.dohvatiOglase().subscribe({
      next: podaci => {
        this.oglasi = podaci;
        this.ucitavanje = false;
      },
      error: greska => {
        this.greska(greska, 'Oglasi trenutno nisu dostupni');
        this.ucitavanje = false;
      }
    });
  }

  private uspeh(poruka: string) {
    this.poruka = poruka;
    this.uspesno = true;
  }
  private greska(greska: any, podrazumevana: string) {
    this.poruka = greska.error?.poruka || podrazumevana;
    this.uspesno = false;
  }
}
