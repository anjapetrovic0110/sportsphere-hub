import { API_URL } from '../services/api.config';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ZaposleniProfil } from '../models/zaposleni-profil';
import { KorisnikServis } from '../services/korisnik.service';
import { PrijavaServis } from '../services/prijava.service';

@Component({
  selector: 'app-zaposleni-profil',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './zaposleni-profil.component.html',
  styleUrl: './zaposleni-profil.component.css'
})
export class ZaposleniProfilKomponenta implements OnInit {
  private servis = inject(KorisnikServis);
  private prijava = inject(PrijavaServis);
  profil = new ZaposleniProfil();
  slika: File | null = null;
  pregled = '';
  poruka = '';
  uspesno = false;
  ucitavanje = true;
  cuvanje = false;
  ngOnInit() {
    this.servis.dohvatiProfilZaposlenog().subscribe({
      next: p => {
        this.profil = p;
        this.ucitavanje = false;
      },
      error: () => {
        this.poruka = 'Profil trenutno nije dostupan';
        this.ucitavanje = false;
      }
    });
  }
  promenaSlike(e: Event) {
    const f = (e.target as HTMLInputElement).files?.[0] || null;
    this.poruka = '';
    if (f && !['image/png', 'image/jpeg'].includes(f.type))
      this.poruka = 'Profilna slika mora biti PNG ili JPG';
    else if (f && f.size > 5 * 1024 * 1024) this.poruka = 'Profilna slika može imati najviše 5 MB';
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
      this.poruka = 'Proverite označena polja';
      return;
    }
    this.cuvanje = true;
    this.servis.izmeniProfilZaposlenog(this.profil, this.slika).subscribe({
      next: p => {
        this.profil = p;
        this.slika = null;
        this.pregled = '';
        const k = this.prijava.dohvatiKorisnika();
        if (k) {
          k.ime = p.ime;
          k.prezime = p.prezime;
          this.prijava.sacuvajKorisnika(k);
        }
        this.poruka = 'Profil je uspešno sačuvan';
        this.uspesno = true;
        this.cuvanje = false;
      },
      error: g => {
        this.poruka = g.error?.poruka || 'Profil nije sačuvan';
        this.cuvanje = false;
      }
    });
  }
  slikaUrl() {
    return (
      this.pregled ||
      (this.profil.urlProfilneSlike ? `${API_URL}${this.profil.urlProfilneSlike}` : '')
    );
  }
  status(s: string) {
    return (
      (
        {
          ACTIVE: 'Aktivan',
          PENDING: 'Čeka odobrenje',
          REJECTED: 'Odbijen',
          INACTIVE: 'Neaktivan'
        } as Record<string, string>
      )[s] || s
    );
  }

  naziviResursa(resursi: string) {
    if (!resursi) return 'Nisu uneti';
    return resursi
      .split(';')
      .map(resurs => resurs.trim().replace(/\s+\([^)]*\)$/, ''))
      .join('; ');
  }
}
