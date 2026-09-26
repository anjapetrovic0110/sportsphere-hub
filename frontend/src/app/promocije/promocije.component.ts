import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ZaposleniPromocija } from '../models/promocija';
import { Sport } from '../models/sport';
import { ZaposleniObjekat, ZaposleniProfil } from '../models/zaposleni-profil';
import { KorisnikServis } from '../services/korisnik.service';
import { RegistracijaServis } from '../services/registracija.service';

@Component({
  selector: 'app-promocije',
  standalone: true,
  imports: [FormsModule, RouterLink, DatePipe, CurrencyPipe],
  templateUrl: './promocije.component.html',
  styleUrl: './promocije.component.css'
})
export class PromocijeKomponenta implements OnInit {
  private servis = inject(KorisnikServis);
  private registracijaServis = inject(RegistracijaServis);
  promocije: ZaposleniPromocija[] = [];
  objekti: ZaposleniObjekat[] = [];
  sportovi: Sport[] = [];
  promocija = new ZaposleniPromocija();
  tipPopusta: 'PERCENT' | 'AMOUNT' = 'PERCENT';
  poruka = '';
  uspesno = false;
  cuvanje = false;
  ngOnInit() {
    this.ucitaj();
    this.servis
      .dohvatiProfilZaposlenog()
      .subscribe({ next: (profil: ZaposleniProfil) => (this.objekti = profil.objekti) });
    this.registracijaServis
      .dohvatiSportove()
      .subscribe({ next: podaci => (this.sportovi = podaci) });
  }

  promeniTip() {
    if (this.tipPopusta === 'PERCENT') this.promocija.iznosPopusta = null;
    else this.promocija.procenatPopusta = null;
  }

  izmeni(promocija: ZaposleniPromocija) {
    this.promocija = { ...promocija };
    this.tipPopusta = promocija.procenatPopusta != null ? 'PERCENT' : 'AMOUNT';
    document.getElementById('forma-promocije')?.scrollIntoView({
      behavior: 'smooth'
    });
  }

  nova() {
    this.promocija = new ZaposleniPromocija();
    this.tipPopusta = 'PERCENT';
    this.poruka = '';
  }

  sacuvaj(forma: NgForm) {
    this.poruka = '';
    this.uspesno = false;
    this.promeniTip();
    if (forma.invalid) {
      forma.control.markAllAsTouched();
      this.poruka = 'Proverite podatke promocije';
      return;
    }
    this.cuvanje = true;
    this.servis.sacuvajPromociju(this.promocija).subscribe({
      next: () => {
        this.poruka = 'Promocija je sačuvana';
        this.uspesno = true;
        this.cuvanje = false;
        this.nova();
        this.poruka = 'Promocija je sačuvana';
        this.uspesno = true;
        this.ucitaj();
      },
      error: greska => {
        this.poruka = greska.error?.poruka || 'Promocija nije sačuvana';
        this.cuvanje = false;
      }
    });
  }

  popust(promocija: ZaposleniPromocija) {
    return promocija.procenatPopusta != null ? `${promocija.procenatPopusta}%` : null;
  }

  private ucitaj() {
    this.servis.dohvatiPromocije().subscribe({
      next: podaci => (this.promocije = podaci),
      error: () => (this.poruka = 'Promocije trenutno nisu dostupne')
    });
  }
}
