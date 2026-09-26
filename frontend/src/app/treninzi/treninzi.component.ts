import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NoviTrening, Trener, Trening } from '../models/trening';
import { KorisnikServis } from '../services/korisnik.service';

@Component({
  selector: 'app-treninzi',
  standalone: true,
  imports: [FormsModule, RouterLink, DatePipe, CurrencyPipe],
  templateUrl: './treninzi.component.html',
  styleUrl: './treninzi.component.css'
})
export class TreninziKomponenta implements OnInit {
  private korisnikServis = inject(KorisnikServis);
  treneri: Trener[] = [];
  sviTreneri: Trener[] = [];
  treninzi: Trening[] = [];
  trening = new NoviTrening();
  idObjekta = 0;
  idSporta = 0;
  poruka = '';
  uspesno = false;
  ucitavanje = true;
  cuvanje = false;
  danas = new Date().toISOString().slice(0, 10);
  ngOnInit() {
    this.korisnikServis.dohvatiTrenere().subscribe({
      next: podaci => {
        this.sviTreneri = podaci;
        this.treneri = podaci;
        this.ucitavanje = false;
      },
      error: greska => {
        this.greska(greska, 'Treneri trenutno nisu dostupni');
        this.ucitavanje = false;
      }
    });
    this.ucitajTreninge();
  }

  objekti() {
    return [...new Map(this.sviTreneri.map(t => [t.idObjekta, t.objekat])).entries()];
  }

  sportovi() {
    return [...new Map(this.sviTreneri.map(t => [t.idSporta, t.sport])).entries()];
  }

  filtriraj() {
    this.korisnikServis
      .dohvatiTrenere(this.idObjekta || undefined, this.idSporta || undefined)
      .subscribe({
        next: podaci => {
          this.treneri = podaci;
          if (!podaci.some(t => t.id === this.trening.idTrenera)) this.trening.idTrenera = 0;
        },
        error: greska => this.greska(greska, 'Pretraga trenera nije uspela')
      });
  }

  izaberi(trener: Trener) {
    this.trening.idTrenera = trener.id;
    this.trening.vremeTreninga = '';
    document.getElementById('zakazivanje')?.scrollIntoView({
      behavior: 'smooth'
    });
  }

  vremena() {
    const trener = this.izabraniTrener();
    if (!trener) return [];
    if (!trener.radnoVremeOd || !trener.radnoVremeDo)
      return Array.from(
        { length: 16 },
        (_, indeks) => `${String(indeks + 7).padStart(2, '0')}:00`
      );
    const od = Number(trener.radnoVremeOd.slice(0, 2));
    const doSat = Number(trener.radnoVremeDo.slice(0, 2));
    const doMinut = Number(trener.radnoVremeDo.slice(3, 5));
    const poslednjiPocetak = doMinut === 0 ? doSat - 1 : doSat;
    return Array.from(
      { length: Math.max(0, poslednjiPocetak - od + 1) },
      (_, indeks) => `${String(od + indeks).padStart(2, '0')}:00`
    );
  }

  zakazi(forma: NgForm) {
    this.poruka = '';
    this.uspesno = false;
    if (forma.invalid) {
      forma.control.markAllAsTouched();
      this.poruka = 'Izaberite trenera i termin';
      return;
    }
    this.cuvanje = true;
    this.korisnikServis.zakaziTrening(this.trening).subscribe({
      next: odgovor => {
        this.poruka = odgovor.poruka;
        this.uspesno = true;
        this.cuvanje = false;
        this.trening.datumTreninga = '';
        this.trening.vremeTreninga = '';
        this.ucitajTreninge();
      },
      error: greska => {
        this.greska(greska, 'Trening nije zakazan');
        this.cuvanje = false;
      }
    });
  }

  nazivStatusa(status: string) {
    return (
      (
        {
          SCHEDULED: 'Zakazan',
          ATTENDED: 'Održan',
          CANCELLED: 'Otkazan',
          NO_SHOW: 'Nedolazak'
        } as Record<string, string>
      )[status] || status
    );
  }

  izabraniTrener() {
    return this.sviTreneri.find(t => t.id === this.trening.idTrenera);
  }

  private ucitajTreninge() {
    this.korisnikServis.dohvatiTreninge().subscribe({
      next: podaci => (this.treninzi = podaci),
      error: greska => this.greska(greska, 'Istorija treninga trenutno nije dostupna')
    });
  }

  private greska(greska: any, podrazumevana: string) {
    this.poruka = greska.error?.poruka || podrazumevana;
    this.uspesno = false;
  }
}
