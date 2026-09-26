import { API_URL } from './api.config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Prijava, PrijavljeniKorisnik } from '../models/prijava';

@Injectable({ providedIn: 'root' })
export class PrijavaServis {
  private httpKlijent = inject(HttpClient);
  adresaServera = API_URL;
  prijava(podaci: Prijava) {
    return this.httpKlijent.post<PrijavljeniKorisnik>(`${this.adresaServera}/prijava`, podaci);
  }

  sacuvajKorisnika(korisnik: PrijavljeniKorisnik) {
    localStorage.setItem('prijavljeniKorisnik', JSON.stringify(korisnik));
  }

  dohvatiKorisnika(): PrijavljeniKorisnik | null {
    const korisnik = localStorage.getItem('prijavljeniKorisnik');
    if (!korisnik) return null;
    try {
      const podaci: PrijavljeniKorisnik = JSON.parse(korisnik);
      let payload = podaci.token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      payload = payload.padEnd(Math.ceil(payload.length / 4) * 4, '=');
      const tokenPodaci = JSON.parse(atob(payload));
      if (!tokenPodaci.exp || tokenPodaci.exp * 1000 <= Date.now()) {
        this.odjava();
        return null;
      }
      return podaci;
    } catch {
      this.odjava();
      return null;
    }
  }

  odjava() {
    localStorage.removeItem('prijavljeniKorisnik');
  }
}
