import { DecimalPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Statistika } from '../models/statistika';
import { KorisnikServis } from '../services/korisnik.service';

@Component({
  selector: 'app-statistika',
  standalone: true,
  imports: [RouterLink, DecimalPipe],
  templateUrl: './statistika.component.html',
  styleUrl: './statistika.component.css'
})
export class StatistikaKomponenta implements OnInit {
  private servis = inject(KorisnikServis);
  podaci: Statistika = { sportovi: [], meseci: [], potrosnja: [] };
  poruka = '';
  ngOnInit() {
    this.servis.dohvatiStatistiku().subscribe({
      next: p => (this.podaci = p),
      error: e => (this.poruka = e.error?.poruka || 'Statistika trenutno nije dostupna')
    });
  }
  maksimumSport() {
    return Math.max(1, ...this.podaci.sportovi.flatMap(s => [s.odigrano, s.rezervisano]));
  }
  maksimumPotrosnja() {
    return Math.max(1, ...this.podaci.potrosnja.map(p => p.ukupno));
  }
  tacke() {
    return this.podaci.meseci
      .map((x, i) => `${this.xPozicija(i)},${this.yPozicija(x.broj)}`)
      .join(' ');
  }
  xPozicija(i: number) {
    if (this.podaci.meseci.length === 1) return 300;
    return 20 + (i * 560) / Math.max(1, this.podaci.meseci.length - 1);
  }
  yPozicija(broj: number) {
    const maksimum = Math.max(1, ...this.podaci.meseci.map(x => x.broj));
    return 180 - (broj * 150) / maksimum;
  }
}
