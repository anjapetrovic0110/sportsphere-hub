import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { AdministratorKorisnik } from '../../models/admin-korisnik';
import { AdministratorTrener } from '../../models/admin-trener';
import { ObjekatZahtev } from '../../models/novi-objekat';
import { RegistracijaPregled } from '../../models/registracija-pregled';
import { AdministratorServis } from '../../services/admin.service';

@Component({
  selector: 'app-registracije',
  standalone: true,
  imports: [FormsModule, CurrencyPipe],
  templateUrl: './registracije.component.html',
  styleUrl: './registracije.component.css'
})
export class RegistracijeKomponenta implements OnInit {
  private adminServis = inject(AdministratorServis);
  zahtevi: RegistracijaPregled[] = [];
  poruka = '';
  objekti: ObjekatZahtev[] = [];
  treneri: AdministratorTrener[] = [];
  nazivSporta = '';
  dodavanjeSporta = false;
  korisnici: AdministratorKorisnik[] = [];
  korisnikZaIzmenu: AdministratorKorisnik | null = null;
  
  ngOnInit(): void {
    this.ucitaj();
    this.ucitajObjekte();
    this.ucitajTrenere();
    this.ucitajKorisnike();
  }
  ucitaj() {
    this.adminServis.dohvatiRegistracije().subscribe({
      next: p => (this.zahtevi = p),
      error: () => (this.poruka = 'Zahtevi nisu dostupni')
    });
  }
  obradi(id: number, odobren: boolean) {
    const poziv = odobren ? this.adminServis.odobri(id) : this.adminServis.odbij(id);
    poziv.subscribe({
      next: o => {
        this.poruka = o.poruka;
        this.ucitaj();
        this.ucitajKorisnike();
      },
      error: g => (this.poruka = g.error?.poruka || 'Obrada nije uspela')
    });
  }
  ucitajObjekte() {
    this.adminServis.dohvatiObjekte().subscribe({ next: o => (this.objekti = o) });
  }
  obradiObjekat(id: number, odobren: boolean) {
    this.adminServis.obradiObjekat(id, odobren).subscribe({
      next: o => {
        this.poruka = o.poruka;
        this.ucitajObjekte();
      },
      error: g => (this.poruka = g.error?.poruka || 'Obrada nije uspela')
    });
  }
  ucitajTrenere() {
    this.adminServis.dohvatiTrenere().subscribe({
      next: podaci => (this.treneri = podaci),
      error: () => (this.poruka = 'Treneri nisu dostupni')
    });
  }
  deaktivirajTrenera(id: number) {
    this.adminServis.deaktivirajTrenera(id).subscribe({
      next: odgovor => {
        this.poruka = odgovor.poruka;
        this.ucitajTrenere();
      },
      error: greska => (this.poruka = greska.error?.poruka || 'Trener nije deaktiviran')
    });
  }
  dodajSport(forma: NgForm) {
    if (forma.invalid) {
      forma.control.markAllAsTouched();
      this.poruka = 'Unesite naziv sporta';
      return;
    }
    this.dodavanjeSporta = true;
    this.adminServis.dodajSport(this.nazivSporta).subscribe({
      next: sport => {
        this.poruka = `Sport ${sport.naziv} je dodat`;
        this.nazivSporta = '';
        forma.resetForm();
        this.dodavanjeSporta = false;
      },
      error: greska => {
        this.poruka = greska.error?.poruka || 'Sport nije dodat';
        this.dodavanjeSporta = false;
      }
    });
  }
  ucitajKorisnike() {
    this.adminServis.dohvatiKorisnike().subscribe({
      next: k => (this.korisnici = k),
      error: () => (this.poruka = 'Korisnici nisu dostupni')
    });
  }
  izaberiKorisnika(k: AdministratorKorisnik) {
    this.korisnikZaIzmenu = { ...k };
  }
  sacuvajKorisnika() {
    if (!this.korisnikZaIzmenu) return;
    this.adminServis.izmeniKorisnika(this.korisnikZaIzmenu).subscribe({
      next: () => {
        this.poruka = 'Korisnik je sačuvan';
        this.korisnikZaIzmenu = null;
        this.ucitajKorisnike();
      },
      error: g => (this.poruka = g.error?.poruka || 'Korisnik nije sačuvan')
    });
  }
  obrisiKorisnika(id: number) {
    this.adminServis.obrisiKorisnika(id).subscribe({
      next: o => {
        this.poruka = o.poruka;
        this.ucitajKorisnike();
      },
      error: g => (this.poruka = g.error?.poruka || 'Nalog nije onemogućen')
    });
  }
}
