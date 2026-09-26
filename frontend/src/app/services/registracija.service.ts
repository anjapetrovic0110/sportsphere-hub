import { API_URL } from './api.config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Registracija } from '../models/registracija';
import { Sport } from '../models/sport';
import { podaciZaServer } from './token.interceptor';

@Injectable({ providedIn: 'root' })
export class RegistracijaServis {
  private httpKlijent = inject(HttpClient);
  adresaServera = API_URL;
  dohvatiSportove() {
    return this.httpKlijent.get<Sport[]>(`${this.adresaServera}/javno/sportovi`);
  }

  registruj(podaci: Registracija, slika: File | null) {
    const forma = new FormData();
    forma.append('podaci', new Blob([JSON.stringify(podaciZaServer(podaci))], { type: 'application/json' }));
    if (slika != null) forma.append('slika', slika);
    return this.httpKlijent.post<{ poruka: string }>(`${this.adresaServera}/javno/registracija`, forma);
  }
}
