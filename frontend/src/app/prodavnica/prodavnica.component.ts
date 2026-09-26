import { API_URL } from '../services/api.config';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Oprema, Porudzbina } from '../models/oprema';
import { Sport } from '../models/sport';
import { KorisnikServis } from '../services/korisnik.service';
import { RegistracijaServis } from '../services/registracija.service';

@Component({
  selector: 'app-prodavnica',
  standalone: true,
  imports: [FormsModule, RouterLink, CurrencyPipe, DatePipe],
  templateUrl: './prodavnica.component.html',
  styleUrl: './prodavnica.component.css'
})
export class ProdavnicaKomponenta implements OnInit {
  private servis = inject(KorisnikServis);
  private registracija = inject(RegistracijaServis);
  artikli: Oprema[] = [];
  sportovi: Sport[] = [];
  porudzbine: Porudzbina[] = [];
  korpa = new Map<number, number>();
  idSporta = 0;
  poruka = '';
  neispravneSlike = new Set<number>();
  ngOnInit() {
    this.ucitaj();
    this.ucitajPorudzbine();
    this.registracija.dohvatiSportove().subscribe({ next: s => (this.sportovi = s) });
  }
  ucitaj() {
    this.servis.dohvatiKatalog(this.idSporta || undefined).subscribe({
      next: a => (this.artikli = a),
      error: () => (this.poruka = 'Katalog nije dostupan')
    });
  }
  dodaj(a: Oprema) {
    const n = this.korpa.get(a.id) || 0;
    if (n < a.zaliha) this.korpa.set(a.id, n + 1);
  }
  ukloni(id: number) {
    const n = this.korpa.get(id) || 0;
    if (n <= 1) this.korpa.delete(id);
    else this.korpa.set(id, n - 1);
  }
  kolicina(id: number) {
    return this.korpa.get(id) || 0;
  }
  ukupno() {
    return this.artikli.reduce((s, a) => s + a.cena * this.kolicina(a.id), 0);
  }
  naruci() {
    const stavke = [...this.korpa].map(([idOpreme, kolicina]) => ({ idOpreme, kolicina }));
    if (!stavke.length) {
      this.poruka = 'Korpa je prazna';
      return;
    }
    this.servis.naruci(stavke).subscribe({
      next: () => {
        this.poruka = 'Porudžbina je kreirana';
        this.korpa.clear();
        this.ucitaj();
        this.ucitajPorudzbine();
      },
      error: g => (this.poruka = g.error?.poruka || 'Poručivanje nije uspelo')
    });
  }
  otkazi(id: number) {
    this.servis.otkaziPorudzbinu(id).subscribe({
      next: o => {
        this.poruka = o.poruka;
        this.ucitaj();
        this.ucitajPorudzbine();
      },
      error: g => (this.poruka = g.error?.poruka || 'Otkazivanje nije uspelo')
    });
  }
  ucitajPorudzbine() {
    this.servis.dohvatiPorudzbine().subscribe({ next: p => (this.porudzbine = p) });
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
  slika(a: Oprema) {
    return a.urlSlike && !this.neispravneSlike.has(a.id)
      ? `${API_URL}${a.urlSlike}`
      : '';
  }

  greskaSlike(id: number) {
    this.neispravneSlike.add(id);
  }
}
