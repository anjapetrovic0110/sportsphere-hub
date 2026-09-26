import { API_URL } from './api.config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { ObjekatDetalji, ObjekatPretraga } from '../models/objekat';
import { PocetnaPodaci } from '../models/pocetna-podaci';

@Injectable({ providedIn: 'root' })
export class JavniServis {
  private httpKlijent = inject(HttpClient);
  adresaServera = `${API_URL}/javno`;
  dohvatiPocetnu() {
    return this.httpKlijent.get<PocetnaPodaci>(`${this.adresaServera}/pocetna`);
  }
  dohvatiGradove() {
    return this.httpKlijent.get<string[]>(`${this.adresaServera}/gradovi`);
  }
  pretraziObjekte(
    naziv: string,
    gradovi: string[],
    idSporta: number | null,
    tip: string,
    slobodnoDanas = false
  ) {
    const parametri: Record<string, string> = {
      naziv,
      gradovi: gradovi.join(','),
      tip,
      slobodnoDanas: String(slobodnoDanas)
    };
    if (idSporta != null) parametri['identifikatorSporta'] = idSporta.toString();
    return this.httpKlijent.get<ObjekatPretraga[]>(`${this.adresaServera}/objekti`, { params: parametri });
  }
  dohvatiObjekat(id: number) {
    return this.httpKlijent.get<ObjekatDetalji>(`${this.adresaServera}/objekti/${id}`);
  }
}
