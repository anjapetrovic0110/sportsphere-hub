import { API_URL } from './api.config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { AdministratorKorisnik } from '../models/admin-korisnik';
import { AdministratorTrener } from '../models/admin-trener';
import { ObjekatZahtev } from '../models/novi-objekat';
import { RegistracijaPregled } from '../models/registracija-pregled';
import { Sport } from '../models/sport';

@Injectable({ providedIn: 'root' })
export class AdministratorServis {
  private httpKlijent = inject(HttpClient);
  adresaServera = `${API_URL}/admin`;
  dohvatiRegistracije() {
    return this.httpKlijent.get<RegistracijaPregled[]>(`${this.adresaServera}/registracije`);
  }
  odobri(id: number) {
    return this.httpKlijent.patch<{ poruka: string }>(`${this.adresaServera}/registracije/${id}/odobri`, {});
  }
  odbij(id: number) {
    return this.httpKlijent.patch<{ poruka: string }>(`${this.adresaServera}/registracije/${id}/odbij`, {});
  }
  dohvatiObjekte() {
    return this.httpKlijent.get<ObjekatZahtev[]>(`${this.adresaServera}/objekti`);
  }
  obradiObjekat(id: number, odobren: boolean) {
    return this.httpKlijent.patch<{ poruka: string }>(
      `${this.adresaServera}/objekti/${id}/${odobren ? 'odobri' : 'odbij'}`,
      {}
    );
  }
  dohvatiTrenere() {
    return this.httpKlijent.get<AdministratorTrener[]>(`${this.adresaServera}/treneri`);
  }
  deaktivirajTrenera(id: number) {
    return this.httpKlijent.patch<{ poruka: string }>(`${this.adresaServera}/treneri/${id}/deaktiviraj`, {});
  }
  dodajSport(naziv: string) {
    return this.httpKlijent.post<Sport>(`${this.adresaServera}/sportovi`, { naziv });
  }
  dohvatiKorisnike() {
    return this.httpKlijent.get<AdministratorKorisnik[]>(`${this.adresaServera}/korisnici`);
  }
  izmeniKorisnika(k: AdministratorKorisnik) {
    return this.httpKlijent.put<AdministratorKorisnik>(`${this.adresaServera}/korisnici/${k.id}`, {
      ime: k.ime,
      prezime: k.prezime,
      telefon: k.telefon,
      imejl: k.imejl,
      status: k.status
    });
  }
  obrisiKorisnika(id: number) {
    return this.httpKlijent.delete<{ poruka: string }>(`${this.adresaServera}/korisnici/${id}`);
  }
}
