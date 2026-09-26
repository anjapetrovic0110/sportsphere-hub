import { API_URL } from './api.config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { NoviObjekat } from '../models/novi-objekat';
import { ReakcijePregled } from '../models/objekat';
import { NoviOglas, Oglas } from '../models/oglas';
import { Oprema, Porudzbina } from '../models/oprema';
import { NovaRezervacija, Profil, RezervacijaPregled, TerminPregled } from '../models/profil';
import { ZaposleniPromocija } from '../models/promocija';
import { Statistika } from '../models/statistika';
import { NoviTrening, Trener, Trening } from '../models/trening';
import { ZaposleniProfil } from '../models/zaposleni-profil';
import { ZaposleniRezervacija } from '../models/zaposleni-rezervacija';
import { ZaposleniTrening } from '../models/zaposleni-trening';
import { podaciZaServer } from './token.interceptor';

@Injectable({ providedIn: 'root' })
export class KorisnikServis {
  private httpKlijent = inject(HttpClient);
  adresaServera = API_URL;
  dohvatiStatus(uloga: string) {
    const putanja = uloga == 'ATHLETE' ? 'sportista' : 'zaposleni';
    return this.httpKlijent.get<{ poruka: string }>(`${this.adresaServera}/${putanja}/status`);
  }

  dohvatiProfil() {
    return this.httpKlijent.get<Profil>(`${this.adresaServera}/sportista/profil`);
  }

  izmeniProfil(profil: Profil, slika: File | null) {
    const forma = new FormData();
    forma.append(
      'podaci',
      new Blob(
        [
          JSON.stringify(podaciZaServer({
            ime: profil.ime,
            prezime: profil.prezime,
            telefon: profil.telefon,
            imejl: profil.imejl,
            idOmiljenihSportova: profil.idOmiljenihSportova
          }))
        ],
        { type: 'application/json' }
      )
    );
    if (slika) forma.append('slika', slika);
    return this.httpKlijent.post<Profil>(`${this.adresaServera}/sportista/profil`, forma);
  }

  dohvatiRezervacije() {
    return this.httpKlijent.get<RezervacijaPregled[]>(`${this.adresaServera}/sportista/rezervacije`);
  }

  otkaziRezervaciju(id: number) {
    return this.httpKlijent.post<{ poruka: string }>(
      `${this.adresaServera}/sportista/rezervacije/${id}/otkazivanje`,
      {}
    );
  }

  dohvatiRaspored(idResursa: number) {
    return this.httpKlijent.get<TerminPregled[]>(
      `${this.adresaServera}/sportista/resursi/${idResursa}/raspored`
    );
  }

  rezervisi(zahtev: NovaRezervacija) {
    return this.httpKlijent.post<{ poruka: string }>(`${this.adresaServera}/sportista/rezervacije`, zahtev);
  }
  dohvatiReakcije(idObjekta: number) {
    return this.httpKlijent.get<ReakcijePregled>(
      `${this.adresaServera}/sportista/objekti/${idObjekta}/reakcije`
    );
  }
  dodajReakciju(idObjekta: number, reakcija: 'LIKE' | 'DISLIKE', komentar: string) {
    return this.httpKlijent.post<ReakcijePregled>(
      `${this.adresaServera}/sportista/objekti/${idObjekta}/reakcije`,
      { reakcija, komentar }
    );
  }
  dohvatiStatistiku() {
    return this.httpKlijent.get<Statistika>(`${this.adresaServera}/sportista/statistika`);
  }

  dohvatiOglase() {
    return this.httpKlijent.get<Oglas[]>(`${this.adresaServera}/sportista/oglasi`);
  }
  dodajOglas(oglas: NoviOglas) {
    return this.httpKlijent.post<{ poruka: string }>(`${this.adresaServera}/sportista/oglasi`, oglas);
  }
  posaljiZahtevZaOglas(id: number) {
    return this.httpKlijent.post<{ poruka: string }>(
      `${this.adresaServera}/sportista/oglasi/${id}/zahtevi`,
      {}
    );
  }
  obradiZahtevZaOglas(oglasId: number, zahtevId: number, akcija: 'prihvati' | 'odbij') {
    return this.httpKlijent.post<{ poruka: string }>(
      `${this.adresaServera}/sportista/oglasi/${oglasId}/zahtevi/${zahtevId}/${akcija}`,
      {}
    );
  }
  zatvoriOglas(id: number) {
    return this.httpKlijent.post<{ poruka: string }>(
      `${this.adresaServera}/sportista/oglasi/${id}/zatvaranje`,
      {}
    );
  }

  dohvatiTrenere(idObjekta?: number, idSporta?: number) {
    const parametri: Record<string, string> = {};
    if (idObjekta) parametri['objekatId'] = String(idObjekta);
    if (idSporta) parametri['identifikatorSporta'] = String(idSporta);
    return this.httpKlijent.get<Trener[]>(`${this.adresaServera}/sportista/treneri`, { params: parametri });
  }
  dohvatiTreninge() {
    return this.httpKlijent.get<Trening[]>(`${this.adresaServera}/sportista/treninzi`);
  }
  zakaziTrening(trening: NoviTrening) {
    return this.httpKlijent.post<{ poruka: string }>(`${this.adresaServera}/sportista/treninzi`, trening);
  }

  dohvatiProfilZaposlenog() {
    return this.httpKlijent.get<ZaposleniProfil>(`${this.adresaServera}/zaposleni/profil`);
  }
  izmeniProfilZaposlenog(profil: ZaposleniProfil, slika: File | null) {
    const forma = new FormData();
    forma.append(
      'podaci',
      new Blob(
        [
          JSON.stringify(podaciZaServer({
            ime: profil.ime,
            prezime: profil.prezime,
            telefon: profil.telefon,
            imejl: profil.imejl,
            idOmiljenihSportova: []
          }))
        ],
        { type: 'application/json' }
      )
    );
    if (slika) forma.append('slika', slika);
    return this.httpKlijent.post<ZaposleniProfil>(`${this.adresaServera}/zaposleni/profil`, forma);
  }
  dodajObjekat(objekat: NoviObjekat) {
    return this.httpKlijent.post<{ poruka: string }>(`${this.adresaServera}/zaposleni/objekti`, objekat);
  }
  uveziObjekat(fajl: File) {
    const forma = new FormData();
    forma.append('fajl', fajl);
    return this.httpKlijent.post<{ poruka: string }>(`${this.adresaServera}/zaposleni/objekti/uvoz`, forma);
  }
  dohvatiObjekatZaposlenog(id: number) {
    return this.httpKlijent.get<NoviObjekat>(`${this.adresaServera}/zaposleni/objekti/${id}`);
  }
  izmeniObjekat(id: number, objekat: NoviObjekat) {
    return this.httpKlijent.put<{ poruka: string }>(`${this.adresaServera}/zaposleni/objekti/${id}`, objekat);
  }
  dohvatiRezervacijeZaposlenog() {
    return this.httpKlijent.get<ZaposleniRezervacija[]>(`${this.adresaServera}/zaposleni/rezervacije`);
  }
  evidentirajRezervaciju(id: number, akcija: 'potvrdi' | 'odjavi') {
    return this.httpKlijent.post<{ poruka: string }>(
      `${this.adresaServera}/zaposleni/rezervacije/${id}/${akcija}`,
      {}
    );
  }
  pomeriRezervaciju(id: number, pocetak: string) {
    return this.httpKlijent.put<{ poruka: string }>(
      `${this.adresaServera}/zaposleni/rezervacije/${id}/termin`,
      { pocetak }
    );
  }
  dohvatiTreningeZaposlenog() {
    return this.httpKlijent.get<ZaposleniTrening[]>(`${this.adresaServera}/zaposleni/treninzi`);
  }
  evidentirajTrening(id: number, akcija: 'potvrdi' | 'odjavi') {
    return this.httpKlijent.post<{ poruka: string }>(
      `${this.adresaServera}/zaposleni/treninzi/${id}/${akcija}`,
      {}
    );
  }
  dohvatiPromocije() {
    return this.httpKlijent.get<ZaposleniPromocija[]>(`${this.adresaServera}/zaposleni/promocije`);
  }
  sacuvajPromociju(promocija: ZaposleniPromocija) {
    const podaci = {
      idObjekta: promocija.idObjekta,
      naziv: promocija.naziv,
      datumOd: promocija.datumOd,
      datumDo: promocija.datumDo,
      procenatPopusta: promocija.procenatPopusta,
      iznosPopusta: promocija.iznosPopusta,
      idSporta: promocija.idSporta,
      aktivan: promocija.aktivan
    };
    return promocija.id
      ? this.httpKlijent.put<ZaposleniPromocija>(
          `${this.adresaServera}/zaposleni/promocije/${promocija.id}`,
          podaci
        )
      : this.httpKlijent.post<ZaposleniPromocija>(`${this.adresaServera}/zaposleni/promocije`, podaci);
  }
  dohvatiOpremu() {
    return this.httpKlijent.get<Oprema[]>(`${this.adresaServera}/zaposleni/oprema`);
  }
  sacuvajOpremu(oprema: Oprema, slika: File | null) {
    const forma = new FormData();
    forma.append(
      'podaci',
      new Blob(
        [
          JSON.stringify(podaciZaServer({
            naziv: oprema.naziv,
            idSporta: oprema.idSporta,
            cena: oprema.cena,
            zaliha: oprema.zaliha,
            aktivan: oprema.aktivan
          }))
        ],
        { type: 'application/json' }
      )
    );
    if (slika) forma.append('slika', slika);
    return oprema.id
      ? this.httpKlijent.put<Oprema>(`${this.adresaServera}/zaposleni/oprema/${oprema.id}`, forma)
      : this.httpKlijent.post<Oprema>(`${this.adresaServera}/zaposleni/oprema`, forma);
  }
  dohvatiKatalog(idSporta?: number) {
    return this.httpKlijent.get<Oprema[]>(`${this.adresaServera}/sportista/prodavnica`, {
      params: idSporta ? { identifikatorSporta: idSporta } : {}
    });
  }
  naruci(stavke: { idOpreme: number; kolicina: number }[]) {
    return this.httpKlijent.post<Porudzbina[]>(`${this.adresaServera}/sportista/porudzbine`, { stavke });
  }
  dohvatiPorudzbine() {
    return this.httpKlijent.get<Porudzbina[]>(`${this.adresaServera}/sportista/porudzbine`);
  }
  otkaziPorudzbinu(id: number) {
    return this.httpKlijent.post<{ poruka: string }>(
      `${this.adresaServera}/sportista/porudzbine/${id}/otkazivanje`,
      {}
    );
  }
  dohvatiPorudzbineZaposlenog() {
    return this.httpKlijent.get<Porudzbina[]>(`${this.adresaServera}/zaposleni/porudzbine`);
  }
  obradiPorudzbinu(id: number, akcija: 'prihvati' | 'preuzmi' | 'otkazi') {
    return this.httpKlijent.post<{ poruka: string }>(
      `${this.adresaServera}/zaposleni/porudzbine/${id}/${akcija}`,
      {}
    );
  }
  izvestaj(tip: 'popunjenost' | 'promet', mesec: string) {
    return this.httpKlijent.get(`${this.adresaServera}/zaposleni/izvestaji/${tip}`, {
      params: { mesec },
      responseType: 'blob'
    });
  }
}
