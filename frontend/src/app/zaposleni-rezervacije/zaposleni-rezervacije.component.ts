import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ZaposleniRezervacija } from '../models/zaposleni-rezervacija';
import { ZaposleniTrening } from '../models/zaposleni-trening';
import { KorisnikServis } from '../services/korisnik.service';

@Component({
  selector: 'app-zaposleni-rezervacije',
  standalone: true,
  imports: [DatePipe, CurrencyPipe, RouterLink],
  templateUrl: './zaposleni-rezervacije.component.html',
  styleUrl: './zaposleni-rezervacije.component.css'
})
export class ZaposleniRezervacijeKomponenta implements OnInit {
  private servis = inject(KorisnikServis);
  rezervacije: ZaposleniRezervacija[] = [];
  treninzi: ZaposleniTrening[] = [];
  poruka = '';
  ngOnInit() {
    this.ucitaj();
    this.ucitajTreninge();
  }

  ucitaj() {
    this.servis.dohvatiRezervacijeZaposlenog().subscribe({
      next: podaci => (this.rezervacije = podaci),
      error: () => (this.poruka = 'Rezervacije nisu dostupne')
    });
  }

  ucitajTreninge() {
    this.servis.dohvatiTreningeZaposlenog().subscribe({
      next: podaci => (this.treninzi = podaci),
      error: () => (this.poruka = 'Individualni treninzi nisu dostupni')
    });
  }

  evidentiraj(id: number, akcija: 'potvrdi' | 'odjavi') {
    this.servis.evidentirajRezervaciju(id, akcija).subscribe({
      next: odgovor => {
        this.poruka = odgovor.poruka;
        this.ucitaj();
      },
      error: greska => (this.poruka = greska.error?.poruka || 'Evidencija nije uspela')
    });
  }

  evidentirajTrening(id: number, akcija: 'potvrdi' | 'odjavi') {
    this.servis.evidentirajTrening(id, akcija).subscribe({
      next: odgovor => {
        this.poruka = odgovor.poruka;
        this.ucitajTreninge();
      },
      error: greska => (this.poruka = greska.error?.poruka || 'Evidencija treninga nije uspela')
    });
  }

  status(status: string) {
    return (
      (
        {
          PENDING: 'Na čekanju',
          CONFIRMED: 'Potvrđena',
          SCHEDULED: 'Zakazan',
          ATTENDED: 'Došao',
          NO_SHOW: 'Nije došao',
          CANCELLED: 'Otkazana'
        } as Record<string, string>
      )[status] || status
    );
  }

  cekaEvidencijuRezervacije(status: string) {
    return status == 'PENDING' || status == 'CONFIRMED';
  }

  cekaEvidencijuTreninga(status: string) {
    return status == 'SCHEDULED';
  }

  porukaProzora(pocetak: string, dostupno: boolean) {
    if (dostupno) return 'Evidencija je sada dostupna';
    const vreme = new Date(pocetak);
    const kraj = new Date(vreme.getTime() + 10 * 60 * 1000);
    const sada = new Date();
    if (sada < vreme) return 'Dostupno do 10 minuta nakon početka termina';
    if (sada > kraj) return 'Rok za evidenciju je istekao';
    return 'Evidencija trenutno nije dostupna';
  }
}
