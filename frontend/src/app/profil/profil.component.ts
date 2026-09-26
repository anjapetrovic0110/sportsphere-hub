import { API_URL } from '../services/api.config';
import { DatePipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Profil, RezervacijaPregled } from '../models/profil';
import { Sport } from '../models/sport';
import { KorisnikServis } from '../services/korisnik.service';
import { PrijavaServis } from '../services/prijava.service';
import { RegistracijaServis } from '../services/registracija.service';

@Component({
  selector: 'app-profil',
  standalone: true,
  imports: [FormsModule, RouterLink, DatePipe],
  templateUrl: './profil.component.html',
  styleUrl: './profil.component.css'
})
export class ProfilKomponenta implements OnInit {
  private korisnikServis = inject(KorisnikServis);
  private registracijaServis = inject(RegistracijaServis);
  private prijavaServis = inject(PrijavaServis);
  profil = new Profil();
  sportovi: Sport[] = [];
  slika: File | null = null;
  pregledSlike = '';
  slikaNeuspesna = false;
  poruka = '';
  uspesno = false;
  ucitavanje = true;
  cuvanje = false;
  rezervacije: RezervacijaPregled[] = [];
  sortiranje = 'pocetak';
  smer: 'asc' | 'desc' = 'desc';
  ngOnInit(): void {
    this.registracijaServis
      .dohvatiSportove()
      .subscribe({ next: podaci => (this.sportovi = podaci) });
    this.korisnikServis.dohvatiProfil().subscribe({
      next: podaci => {
        this.profil = podaci;
        this.slikaNeuspesna = false;
        this.ucitavanje = false;
      },
      error: () => {
        this.poruka = 'Profil trenutno nije dostupan';
        this.ucitavanje = false;
      }
    });
    this.ucitajRezervacije();
  }

  promenaSporta(id: number, izabran: boolean) {
    if (izabran && !this.profil.idOmiljenihSportova.includes(id)) {
      if (this.profil.idOmiljenihSportova.length < 5) this.profil.idOmiljenihSportova.push(id);
    } else if (!izabran)
      this.profil.idOmiljenihSportova = this.profil.idOmiljenihSportova.filter(x => x !== id);
  }

  promenaSlike(dogadjaj: Event) {
    const input = dogadjaj.target as HTMLInputElement;
    const fajl = input.files?.[0] || null;
    this.poruka = '';
    if (fajl && !['image/png', 'image/jpeg'].includes(fajl.type))
      this.poruka = 'Profilna slika mora biti PNG ili JPG';
    else if (fajl && fajl.size > 5 * 1024 * 1024)
      this.poruka = 'Profilna slika može imati najviše 5 MB';
    else {
      this.slika = fajl;
      this.slikaNeuspesna = false;
      if (this.pregledSlike) URL.revokeObjectURL(this.pregledSlike);
      this.pregledSlike = fajl ? URL.createObjectURL(fajl) : '';
    }
  }

  sacuvaj(forma: NgForm) {
    this.poruka = '';
    this.uspesno = false;
    if (forma.invalid) {
      forma.control.markAllAsTouched();
      this.poruka = 'Proverite označena polja';
      return;
    }
    this.cuvanje = true;
    this.korisnikServis.izmeniProfil(this.profil, this.slika).subscribe({
      next: podaci => {
        this.profil = podaci;
        this.slika = null;
        this.pregledSlike = '';
        this.slikaNeuspesna = false;
        const prijavljen = this.prijavaServis.dohvatiKorisnika();
        if (prijavljen) {
          prijavljen.ime = podaci.ime;
          prijavljen.prezime = podaci.prezime;
          this.prijavaServis.sacuvajKorisnika(prijavljen);
        }
        this.poruka = 'Profil je uspešno sačuvan';
        this.uspesno = true;
        this.cuvanje = false;
      },
      error: greska => {
        this.poruka = greska.error?.poruka || 'Profil nije sačuvan';
        this.cuvanje = false;
      }
    });
  }

  slikaUrl() {
    return (
      this.pregledSlike ||
      (this.profil.urlProfilneSlike ? `${API_URL}${this.profil.urlProfilneSlike}` : '')
    );
  }

  sortiraj(polje: keyof RezervacijaPregled) {
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

  otkazi(rezervacija: RezervacijaPregled) {
    this.poruka = '';
    this.korisnikServis.otkaziRezervaciju(rezervacija.id).subscribe({
      next: odgovor => {
        this.poruka = odgovor.poruka;
        this.uspesno = true;
        this.ucitajRezervacije();
      },
      error: greska => {
        this.poruka = greska.error?.poruka || 'Rezervacija nije otkazana';
        this.uspesno = false;
      }
    });
  }

  nazivStatusa(status: string) {
    return (
      (
        {
          PENDING: 'Na čekanju',
          CONFIRMED: 'Potvrđena',
          CANCELLED: 'Otkazana',
          ATTENDED: 'Realizovana',
          NO_SHOW: 'Nedolazak'
        } as Record<string, string>
      )[status] || status
    );
  }

  private ucitajRezervacije() {
    this.korisnikServis.dohvatiRezervacije().subscribe({
      next: podaci => {
        this.rezervacije = podaci;
        this.primeniSortiranje();
      },
      error: () => (this.poruka = 'Rezervacije trenutno nisu dostupne')
    });
  }

  private primeniSortiranje() {
    const polje = this.sortiranje as keyof RezervacijaPregled;
    const faktor = this.smer === 'asc' ? 1 : -1;
    this.rezervacije = [...this.rezervacije].sort(
      (a, b) => String(a[polje]).localeCompare(String(b[polje]), 'sr') * faktor
    );
  }
}
