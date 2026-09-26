export interface Trener {
  id: number;
  ime: string;
  specijalizacija: string;
  prosecnaOcena: number;
  cenaPoSatu: number;
  idObjekta: number;
  objekat: string;
  idSporta: number;
  sport: string;
  radnoVremeOd?: string;
  radnoVremeDo?: string;
}

export class NoviTrening {
  idTrenera = 0;
  datumTreninga = '';
  vremeTreninga = '';
}

export interface Trening {
  id: number;
  trener: string;
  objekat: string;
  grad: string;
  sport: string;
  pocetak: string;
  kraj: string;
  status: string;
  cena: number;
}
