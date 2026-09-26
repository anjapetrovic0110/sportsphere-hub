import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NoviObjekat, NoviResurs } from '../models/novi-objekat';
import { Sport } from '../models/sport';
import { KorisnikServis } from '../services/korisnik.service';
import { RegistracijaServis } from '../services/registracija.service';

@Component({
  selector: 'app-novi-objekat',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './novi-objekat.component.html',
  styleUrl: './novi-objekat.component.css'
})
export class NoviObjekatKomponenta implements OnInit {
  private servis = inject(KorisnikServis);
  private sportServis = inject(RegistracijaServis);
  private ruta = inject(ActivatedRoute);
  idObjekta: number | null = null;
  objekat = new NoviObjekat();
  sportovi: Sport[] = [];
  poruka = '';
  uspesno = false;
  slanje = false;
  ngOnInit() {
    this.sportServis.dohvatiSportove().subscribe({ next: s => (this.sportovi = s) });
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    if (id) {
      this.idObjekta = id;
      this.servis.dohvatiObjekatZaposlenog(id).subscribe({
        next: o => (this.objekat = o),
        error: g => (this.poruka = g.error?.poruka || 'Objekat nije dostupan')
      });
    }
  }
  dodajResurs() {
    this.objekat.resursi.push(new NoviResurs());
  }
  ukloniResurs(i: number) {
    if (this.objekat.resursi.length > 1) this.objekat.resursi.splice(i, 1);
  }
  sport(r: NoviResurs, id: number, izabran: boolean) {
    r.idSportova = izabran ? [...r.idSportova, id] : r.idSportova.filter(x => x !== id);
  }

  sacuvaj(forma: NgForm) {
    this.poruka = '';
    this.uspesno = false;
    const greske = this.greskeValidacije();
    if (forma.invalid || greske.length) {
      forma.control.markAllAsTouched();
      this.poruka = greske.join(' ');
      return;
    }
    this.slanje = true;
    const poziv = this.idObjekta
      ? this.servis.izmeniObjekat(this.idObjekta, this.objekat)
      : this.servis.dodajObjekat(this.objekat);
    poziv.subscribe({
      next: o => {
        this.poruka = o.poruka;
        this.uspesno = true;
        if (!this.idObjekta) this.objekat = new NoviObjekat();
        this.slanje = false;
      },
      error: g => {
        this.poruka = g.error?.poruka || 'Objekat nije sačuvan';
        this.slanje = false;
      }
    });
  }

  private greskeValidacije() {
    const greske: string[] = [];
    if (!this.objekat.naziv.trim()) greske.push('Unesite naziv objekta.');
    if (!this.objekat.grad.trim()) greske.push('Unesite grad objekta.');
    if (!this.objekat.adresa.trim()) greske.push('Unesite adresu objekta.');
    if (!this.objekat.vremeOtvaranja) greske.push('Izaberite vreme otvaranja.');
    if (!this.objekat.vremeZatvaranja) greske.push('Izaberite vreme zatvaranja.');
    if (this.objekat.dozvoljeniNedolasci < 1)
      greske.push('Broj nedolazaka pre zabrane mora biti najmanje 1.');
    if (!this.objekat.resursi.length) greske.push('Dodajte najmanje jedan resurs.');
    this.objekat.resursi.forEach((resurs, indeks) => {
      const prefiks = `Resurs ${indeks + 1}:`;
      if (!resurs.naziv.trim()) greske.push(`${prefiks} unesite naziv.`);
      if (!resurs.tip) greske.push(`${prefiks} izaberite tip.`);
      if (resurs.kapacitet == null || resurs.kapacitet < 1)
        greske.push(`${prefiks} kapacitet mora biti najmanje 1.`);
      if (resurs.cenaPoSatu == null || resurs.cenaPoSatu < 0)
        greske.push(`${prefiks} cena ne može biti negativna.`);
      if (!resurs.idSportova.length) greske.push(`${prefiks} izaberite najmanje jedan sport.`);
    });
    return greske;
  }

  uvezi(dogadjaj: Event) {
    const fajl = (dogadjaj.target as HTMLInputElement).files?.[0];
    if (!fajl) return;
    this.poruka = '';
    this.uspesno = false;
    this.slanje = true;
    this.servis.uveziObjekat(fajl).subscribe({
      next: o => {
        this.poruka = o.poruka;
        this.uspesno = true;
        this.slanje = false;
      },
      error: g => {
        this.poruka = g.error?.poruka || 'Uvoz nije uspeo';
        this.slanje = false;
      }
    });
  }
}
