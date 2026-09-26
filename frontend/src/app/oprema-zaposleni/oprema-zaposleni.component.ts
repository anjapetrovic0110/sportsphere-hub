import { API_URL } from '../services/api.config';
import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Oprema } from '../models/oprema';
import { Sport } from '../models/sport';
import { KorisnikServis } from '../services/korisnik.service';
import { RegistracijaServis } from '../services/registracija.service';

@Component({
  selector: 'app-oprema-zaposleni',
  standalone: true,
  imports: [FormsModule, RouterLink, CurrencyPipe],
  templateUrl: './oprema-zaposleni.component.html',
  styleUrl: './oprema-zaposleni.component.css'
})
export class OpremaZaposleniKomponenta implements OnInit {
  private servis = inject(KorisnikServis);
  private registracija = inject(RegistracijaServis);
  oprema: Oprema[] = [];
  sportovi: Sport[] = [];
  stavka = new Oprema();
  slika: File | null = null;
  neispravneSlike = new Set<number>();
  pregled = '';
  poruka = '';
  uspesno = false;
  cuvanje = false;
  ngOnInit() {
    this.ucitaj();
    this.registracija.dohvatiSportove().subscribe({ next: s => (this.sportovi = s) });
  }
  ucitaj() {
    this.servis.dohvatiOpremu().subscribe({
      next: o => {
        this.oprema = o;
        this.neispravneSlike.clear();
      },
      error: () => (this.poruka = 'Katalog nije dostupan')
    });
  }
  izmeni(o: Oprema) {
    this.stavka = { ...o };
    this.slika = null;
    this.pregled = '';
    scrollTo({ top: 0, behavior: 'smooth' });
  }
  nova() {
    this.stavka = new Oprema();
    this.slika = null;
    this.pregled = '';
  }
  promenaSlike(e: Event) {
    const f = (e.target as HTMLInputElement).files?.[0] || null;
    this.poruka = '';
    if (f && !['image/png', 'image/jpeg'].includes(f.type))
      this.poruka = 'Slika mora biti PNG ili JPG';
    else if (f && f.size > 5 * 1024 * 1024) this.poruka = 'Slika može imati najviše 5 MB';
    else {
      this.slika = f;
      this.pregled = f ? URL.createObjectURL(f) : '';
    }
  }
  sacuvaj(f: NgForm) {
    this.poruka = '';
    this.uspesno = false;
    if (f.invalid) {
      f.control.markAllAsTouched();
      this.poruka = 'Proverite podatke opreme';
      return;
    }
    this.cuvanje = true;
    this.servis.sacuvajOpremu(this.stavka, this.slika).subscribe({
      next: () => {
        this.poruka = 'Oprema je sačuvana';
        this.uspesno = true;
        this.cuvanje = false;
        this.nova();
        this.ucitaj();
      },
      error: g => {
        this.poruka = g.error?.poruka || 'Oprema nije sačuvana';
        this.cuvanje = false;
      }
    });
  }
  slikaUrl(o: Oprema) {
    return o.urlSlike && !this.neispravneSlike.has(o.id)
      ? `${API_URL}${o.urlSlike}`
      : '';
  }

  greskaSlike(id: number) {
    this.neispravneSlike.add(id);
  }
}
