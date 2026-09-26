export class NoviOglas {
  idSporta = 0;
  grad = '';
  datumIgre = '';
  vremeIgre = '';
  nedostajeIgraca = 1;
}

export interface ZahtevZaOglas {
  id: number;
  sportista: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
}

export interface Oglas {
  id: number;
  autor: string;
  sport: string;
  grad: string;
  datum: string;
  vreme: string;
  nedostaje: number;
  prihvaceno: number;
  moj: boolean;
  poslatZahtev: boolean;
  zahtevi: ZahtevZaOglas[];
}
