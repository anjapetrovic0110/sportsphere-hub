import { DecimalPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ObjekatPretraga } from '../models/objekat';
import { Sport } from '../models/sport';
import { JavniServis } from '../services/javni.service';
import { PrijavaServis } from '../services/prijava.service';
import { RegistracijaServis } from '../services/registracija.service';

@Component({
  selector: 'app-objekti',
  standalone: true,
  imports: [FormsModule, RouterLink, DecimalPipe],
  templateUrl: './objekti.component.html',
  styleUrl: './objekti.component.css'
})
export class ObjektiKomponenta implements OnInit {
  private javniServis = inject(JavniServis);
  private registracijaServis = inject(RegistracijaServis);
  private prijavaServis = inject(PrijavaServis);
  naziv = '';
  izabraniGradovi: string[] = [];
  idSporta: number | null = null;
  tip = '';
  slobodnoDanas = false;
  korisnik = this.prijavaServis.dohvatiKorisnika();
  gradovi: string[] = [];
  sportovi: Sport[] = [];
  objekti: ObjekatPretraga[] = [];
  ucitavanje = false;
  poruka = '';
  sortiranje = '';
  smer: 'asc' | 'desc' = 'asc';
  ngOnInit(): void {
    this.javniServis.dohvatiGradove().subscribe({ next: podaci => (this.gradovi = podaci) });
    this.registracijaServis
      .dohvatiSportove()
      .subscribe({ next: podaci => (this.sportovi = podaci) });
    this.pretrazi();
  }

  pretrazi() {
    this.ucitavanje = true;
    this.poruka = '';
    this.javniServis
      .pretraziObjekte(
        this.naziv,
        this.izabraniGradovi,
        this.idSporta,
        this.tip,
        this.slobodnoDanas
      )
      .subscribe({
        next: podaci => {
          this.objekti = podaci;
          this.ucitavanje = false;
          if (this.sortiranje) this.primeniSortiranje();
        },
        error: greska => {
          this.poruka = greska.error?.poruka || 'Pretraga trenutno nije dostupna';
          this.ucitavanje = false;
        }
      });
  }

  sortiraj(polje: 'naziv' | 'grad' | 'sportovi') {
    if (this.sortiranje === polje) this.smer = this.smer === 'asc' ? 'desc' : 'asc';
    else {
      this.sortiranje = polje;
      this.smer = 'asc';
    }
    this.primeniSortiranje();
  }

  oznaka(polje: string) {
    return this.sortiranje === polje ? (this.smer === 'asc' ? ' ↑' : ' ↓') : '';
  }

  private primeniSortiranje() {
    const polje = this.sortiranje as keyof ObjekatPretraga;
    const faktor = this.smer === 'asc' ? 1 : -1;
    this.objekti = [...this.objekti].sort(
      (a, b) => String(a[polje]).localeCompare(String(b[polje]), 'sr') * faktor
    );
  }
}
