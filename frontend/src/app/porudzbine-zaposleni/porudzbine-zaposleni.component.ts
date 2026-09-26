import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Porudzbina } from '../models/oprema';
import { KorisnikServis } from '../services/korisnik.service';

@Component({
  selector: 'app-porudzbine-zaposleni',
  standalone: true,
  imports: [RouterLink, CurrencyPipe, DatePipe],
  templateUrl: './porudzbine-zaposleni.component.html',
  styleUrl: './porudzbine-zaposleni.component.css'
})
export class PorudzbineZaposleniKomponenta implements OnInit {
  private servis = inject(KorisnikServis);
  porudzbine: Porudzbina[] = [];
  poruka = '';
  ngOnInit() {
    this.ucitaj();
  }
  ucitaj() {
    this.servis.dohvatiPorudzbineZaposlenog().subscribe({
      next: p => (this.porudzbine = p),
      error: () => (this.poruka = 'Porudžbine nisu dostupne')
    });
  }
  obradi(id: number, akcija: 'prihvati' | 'preuzmi' | 'otkazi') {
    this.servis.obradiPorudzbinu(id, akcija).subscribe({
      next: o => {
        this.poruka = o.poruka;
        this.ucitaj();
      },
      error: g => (this.poruka = g.error?.poruka || 'Promena statusa nije uspela')
    });
  }
  status(s: string) {
    return (
      (
        {
          ORDERED: 'Naručeno',
          ACCEPTED: 'Prihvaćeno',
          PICKED_UP: 'Preuzeto',
          CANCELLED: 'Otkazano'
        } as Record<string, string>
      )[s] || s
    );
  }
}
