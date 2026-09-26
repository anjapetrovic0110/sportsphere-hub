export class Oprema {
  id = 0;
  naziv = '';
  idSporta = 0;
  sport = '';
  cena = 0;
  zaliha = 0;
  aktivan = true;
  urlSlike = '';
}
export interface StavkaPorudzbine {
  naziv: string;
  kolicina: number;
  cena: number;
}
export interface Porudzbina {
  id: number;
  organizacija: string;
  sportista: string;
  status: string;
  ukupno: number;
  kreirano: string;
  stavke: StavkaPorudzbine[];
}
