import { DatePipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ZaposleniRezervacija } from '../models/zaposleni-rezervacija';
import { ZaposleniTrening } from '../models/zaposleni-trening';
import { KorisnikServis } from '../services/korisnik.service';

@Component({
  selector: 'app-zaposleni-kalendar',
  standalone: true,
  imports: [DatePipe, FormsModule, RouterLink],
  templateUrl: './zaposleni-kalendar.component.html',
  styleUrl: './zaposleni-kalendar.component.css'
})
export class ZaposleniKalendarKomponenta implements OnInit {
  private servis = inject(KorisnikServis);
  rezervacije: ZaposleniRezervacija[] = [];
  treninzi: ZaposleniTrening[] = [];
  izabraniObjekat = '';
  izabraniResurs = '';
  poruka = '';
  sati = Array.from({ length: 17 }, (_, i) => i + 6);
  dani = Array.from({ length: 7 }, (_, i) => {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    d.setDate(d.getDate() + i);
    return d;
  });
  prevucena: ZaposleniRezervacija | null = null;
  ngOnInit() {
    this.servis.dohvatiTreningeZaposlenog().subscribe({
      next: t => {
        this.treninzi = t;
        if (!this.izabraniObjekat) {
          this.izabraniObjekat = this.objekti()[0] || '';
          this.promenaObjekta();
        }
      }
    });
    this.servis.dohvatiRezervacijeZaposlenog().subscribe({
      next: r => {
        this.rezervacije = r;
        this.izabraniObjekat = this.objekti()[0] || '';
        this.promenaObjekta();
      },
      error: () => (this.poruka = 'Kalendar trenutno nije dostupan')
    });
  }
  objekti() {
    return [
      ...new Set([...this.rezervacije.map(r => r.objekat), ...this.treninzi.map(t => t.objekat)])
    ].sort();
  }
  resursi() {
    const r = [
      ...new Set(
        this.rezervacije.filter(x => x.objekat === this.izabraniObjekat).map(x => x.resurs)
      )
    ].sort();
    if (this.treninzi.some(t => t.objekat === this.izabraniObjekat))
      r.push('Individualni treninzi');
    return r;
  }
  promenaObjekta() {
    const pocetak = this.dani[0];
    const kraj = new Date(pocetak);
    kraj.setDate(kraj.getDate() + 7);
    const rezervacija = this.rezervacije.find(
      r =>
        r.objekat === this.izabraniObjekat &&
        r.status !== 'CANCELLED' &&
        new Date(r.pocetak) < kraj &&
        new Date(r.kraj) > pocetak
    );
    const imaTrening = this.treninzi.some(
      t =>
        t.objekat === this.izabraniObjekat &&
        t.status !== 'CANCELLED' &&
        new Date(t.pocetak) < kraj &&
        new Date(t.kraj) > pocetak
    );
    this.izabraniResurs =
      rezervacija?.resurs || (imaTrening ? 'Individualni treninzi' : this.resursi()[0] || '');
  }
  imaTerminaUNedelji() {
    return this.dani.some(dan => this.sati.some(sat => !!this.termin(dan, sat)));
  }
  termin(dan: Date, sat: number) {
    const od = new Date(dan);
    od.setHours(sat, 0, 0, 0);
    const doVreme = new Date(od.getTime() + 3600000);
    if (this.izabraniResurs === 'Individualni treninzi')
      return this.treninzi.find(
        t =>
          t.objekat === this.izabraniObjekat &&
          t.status !== 'CANCELLED' &&
          new Date(t.pocetak) < doVreme &&
          new Date(t.kraj) > od
      );
    return this.rezervacije.find(
      r =>
        r.objekat === this.izabraniObjekat &&
        r.resurs === this.izabraniResurs &&
        r.status !== 'CANCELLED' &&
        new Date(r.pocetak) < doVreme &&
        new Date(r.kraj) > od
    );
  }
  klasa(r: ZaposleniRezervacija | ZaposleniTrening | undefined) {
    return r ? r.status.toLowerCase() : '';
  }
  naslov(r: ZaposleniRezervacija | ZaposleniTrening) {
    return 'trener' in r ? `${r.sportista} · ${r.trener}` : r.sportista;
  }
  mozePomeranje(r: ZaposleniRezervacija | ZaposleniTrening) {
    return (
      !('trener' in r) &&
      r.tip !== 'OUTDOOR_FIELD' &&
      ['PENDING', 'CONFIRMED'].includes(r.status) &&
      new Date(r.pocetak) > new Date()
    );
  }
  zapocniPrevucenje(r: ZaposleniRezervacija | ZaposleniTrening, dogadjaj: DragEvent) {
    if (!this.mozePomeranje(r)) {
      dogadjaj.preventDefault();
      return;
    }
    this.prevucena = r as ZaposleniRezervacija;
    dogadjaj.dataTransfer?.setData('text/plain', String(r.id));
  }
  dozvoliSpustanje(dan: Date, sat: number, dogadjaj: DragEvent) {
    if (this.prevucena && !this.termin(dan, sat) && this.vreme(dan, sat) > new Date())
      dogadjaj.preventDefault();
  }
  spusti(dan: Date, sat: number) {
    if (!this.prevucena || this.termin(dan, sat)) return;
    const datum = this.vreme(dan, sat);
    if (datum <= new Date()) return;
    const lokalno = `${datum.getFullYear()}-${String(datum.getMonth() + 1).padStart(
      2,
      '0'
    )}-${String(datum.getDate()).padStart(2, '0')}T${String(sat).padStart(2, '0')}:00:00`;
    const id = this.prevucena.id;
    this.prevucena = null;
    this.servis.pomeriRezervaciju(id, lokalno).subscribe({
      next: o => {
        this.poruka = o.poruka;
        this.ucitajRezervacije();
      },
      error: e => (this.poruka = e.error?.poruka || 'Termin nije pomeren')
    });
  }
  private vreme(dan: Date, sat: number) {
    const d = new Date(dan);
    d.setHours(sat, 0, 0, 0);
    return d;
  }
  private ucitajRezervacije() {
    this.servis.dohvatiRezervacijeZaposlenog().subscribe({
      next: r => (this.rezervacije = r),
      error: () => (this.poruka = 'Kalendar trenutno nije dostupan')
    });
  }
}
