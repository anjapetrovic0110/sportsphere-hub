import { API_URL } from './api.config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LozinkaServis {
  private httpKlijent = inject(HttpClient);
  private url = `${API_URL}/javno/lozinka`;
  zahtev(identifikator: string) {
    return this.httpKlijent.post<{
      poruka: string;
    }>(`${this.url}/zahtev`, { identifikator });
  }

  promeni(token: string, lozinka: string) {
    return this.httpKlijent.post<{ poruka: string }>(`${this.url}/reset`, { token, lozinka });
  }
}
