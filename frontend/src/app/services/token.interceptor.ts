import { HttpEvent, HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { map } from 'rxjs';
import { isApiRequest } from './api.config';
import { inject } from '@angular/core';
import { PrijavaServis } from './prijava.service';

const naziviZaAplikaciju: Record<string, string> = {
  email: 'imejl',
  identifikatoriOmiljenihSportova: 'idOmiljenihSportova',
  registrovanaAdresa: 'adresaSedista', poreskiBroj: 'pib',
  identifikatorSporta: 'idSporta', identifikatoriSportova: 'idSportova',
  identifikatorTrenera: 'idTrenera', identifikatorResursa: 'idResursa',
  identifikatorObjekta: 'idObjekta', identifikatorOpreme: 'idOpreme',
  nedostajuciIgraci: 'nedostajeIgraca',
  datumPocetka: 'datumOd', datumZavrsetka: 'datumDo',
  nazivOpreme: 'naziv'
};

const naziviZaServer: Record<string, string> = {
  imejl: 'email', idOmiljenihSportova: 'identifikatoriOmiljenihSportova',
  adresaSedista: 'registrovanaAdresa', pib: 'poreskiBroj',
  idSporta: 'identifikatorSporta', idSportova: 'identifikatoriSportova',
  idTrenera: 'identifikatorTrenera', idResursa: 'identifikatorResursa',
  idObjekta: 'identifikatorObjekta', idOpreme: 'identifikatorOpreme',
  nedostajeIgraca: 'nedostajuciIgraci', datumTreninga: 'datum',
  vremeTreninga: 'vreme', datumOd: 'datumPocetka', datumDo: 'datumZavrsetka'
};

function prilagodiNazive(vrednost: unknown, nazivi: Record<string, string>): unknown {
  if (Array.isArray(vrednost)) return vrednost.map(stavka => prilagodiNazive(stavka, nazivi));
  if (vrednost == null || typeof vrednost !== 'object' || vrednost instanceof Blob || vrednost instanceof FormData)
    return vrednost;
  return Object.fromEntries(
    Object.entries(vrednost as Record<string, unknown>).map(([kljuc, podatak]) => [
      nazivi[kljuc] || kljuc,
      prilagodiNazive(podatak, nazivi)
    ])
  );
}

export function podaciZaServer<T>(vrednost: T): unknown {
  return prilagodiNazive(vrednost, naziviZaServer);
}

export const tokenInterceptor: HttpInterceptorFn = (zahtev, prosledi) => {
  if (!isApiRequest(zahtev.url)) return prosledi(zahtev);
  if (zahtev.body && !(zahtev.body instanceof FormData))
    zahtev = zahtev.clone({ body: podaciZaServer(zahtev.body) });
  const korisnik = inject(PrijavaServis).dohvatiKorisnika();
  if (korisnik?.token) {
    zahtev = zahtev.clone({ setHeaders: { Authorization: `Bearer ${korisnik.token}` } });
  }
  return prosledi(zahtev).pipe(
    map((dogadjaj: HttpEvent<unknown>) =>
      dogadjaj instanceof HttpResponse
        ? dogadjaj.clone({ body: prilagodiNazive(dogadjaj.body, naziviZaAplikaciju) })
        : dogadjaj
    )
  );
};
